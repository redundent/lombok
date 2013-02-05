/*
 * Copyright (C) 2010-2012 The Project Lombok Authors.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package lombok.eclipse.handlers;

import static lombok.eclipse.Eclipse.ECLIPSE_DO_NOT_TOUCH_FLAG;
import static lombok.eclipse.handlers.EclipseHandlerUtil.*;

import java.util.ArrayList;
import java.util.List;

import lombok.OneToMany;
import lombok.OneToOne;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.data.OneToManyRelation;
import lombok.core.data.OneToOneRelation;
import lombok.eclipse.DeferUntilBuildFieldsAndMethods;
import lombok.eclipse.Eclipse;
import lombok.eclipse.EclipseAnnotationHandler;
import lombok.eclipse.EclipseNode;
import lombok.eclipse.handlers.EclipseHandlerUtil.MemberExistsResult;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedAllocationExpression;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ReturnStatement;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.mangosdk.spi.ProviderFor;

public class HandleRelations {
	@ProviderFor(EclipseAnnotationHandler.class)
	public static class HandleOneToOne extends EclipseAnnotationHandler<OneToOne> {
		@Override public void handle(AnnotationValues<OneToOne> annotation, Annotation ast, EclipseNode annotationNode) {
			HandleRelations.handle(annotation, ast, annotationNode);
		}
	}
	
	@ProviderFor(EclipseAnnotationHandler.class)
	@DeferUntilBuildFieldsAndMethods
	public static class HanldeOneToMany extends EclipseAnnotationHandler<OneToMany> {
		@Override public void handle(AnnotationValues<OneToMany> annotation, Annotation ast, EclipseNode annotationNode) {

			HandleRelations.handle(annotation, ast, annotationNode);
		}
	}
	
	private static void handle(AnnotationValues<?> annotation, Annotation ast, EclipseNode annotationNode) {
		Object anno = annotation.getInstance();
		
		EclipseNode fieldNode = annotationNode.up();
		EclipseNode typeNode = fieldNode.up();
		
		if (fieldNode.getKind() != Kind.FIELD) {
			annotationNode.addError("@" + anno.getClass().getSimpleName() + " is only supported on a field.");
			return;
		}
		
		FieldDeclaration field = (FieldDeclaration) fieldNode.get();
		
		if (field.isStatic()) {
			annotationNode.addError("@" + anno.getClass().getSimpleName() + " is not legal on static fields.");
			return;
		}
		
		if (fieldExists(toProperCase(new String(field.name)), fieldNode) == MemberExistsResult.NOT_EXISTS) {
			FieldDeclaration fieldDecl = createField(anno, fieldNode, ast);
			injectFieldSuppressWarnings(typeNode, fieldDecl);
			
			typeNode.rebuild();
		}
	}
	
	private static FieldDeclaration createField(Object anno, EclipseNode fieldNode, ASTNode source) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		FieldDeclaration result = null;
		FieldDeclaration fieldDecl = (FieldDeclaration) fieldNode.get();
		
		String relatedFieldName = null;
		boolean isOneToOne = false;
		boolean isUnique = false;

		String baseTypeName = new String(((TypeDeclaration) fieldNode.up().get()).name);

		char[] qualifiedRelationTypeName = null;
		char[] singleRelationTypeName = null;
		TypeReference fieldType = null;
		TypeReference baseType = createTypeReference(baseTypeName.split("\\."), p);
		setGeneratedBy(baseType, source);
		TypeReference referenceType = fieldDecl.type;
		
		if (anno instanceof OneToOne) {
			isOneToOne = true;
			relatedFieldName = ((OneToOne) anno).field();

			qualifiedRelationTypeName = OneToOneRelation.class.getName().toCharArray();
			singleRelationTypeName = OneToOneRelation.class.getSimpleName().toCharArray();
		} else {
			relatedFieldName = ((OneToMany) anno).field();
			isUnique = ((OneToMany) anno).unique();
			
			if (referenceType instanceof ParameterizedSingleTypeReference) {
				referenceType = ((ParameterizedSingleTypeReference) referenceType).typeArguments[0];
			} else if (referenceType instanceof ParameterizedQualifiedTypeReference) {
				ParameterizedQualifiedTypeReference type = (ParameterizedQualifiedTypeReference) referenceType;
				referenceType = type.typeArguments[type.typeArguments.length - 1][0];
			}
			
			qualifiedRelationTypeName = OneToManyRelation.class.getName().toCharArray();
			singleRelationTypeName = OneToManyRelation.class.getSimpleName().toCharArray();
		}
		
		addImportIfNotExists((CompilationUnitDeclaration) fieldNode.top().get(), qualifiedRelationTypeName, source);
		
		fieldType = new ParameterizedSingleTypeReference(singleRelationTypeName, new TypeReference[] { baseType, referenceType }, 0, p);
		setGeneratedBy(fieldType, source);
		fieldType.sourceStart = pS; fieldType.sourceEnd = fieldType.statementEnd = pE;
		
		CompilationResult compResult = ((CompilationUnitDeclaration) fieldNode.top().get()).compilationResult;
		
		final TypeDeclaration typeDeclaration = new TypeDeclaration(compResult);
		setGeneratedBy(typeDeclaration, source);
		typeDeclaration.name = CharOperation.NO_CHAR;
		typeDeclaration.bits |= (ASTNode.IsAnonymousType | ASTNode.IsLocalType);
		typeDeclaration.bodyStart = source.sourceStart;
		typeDeclaration.bodyEnd = source.sourceEnd;
		typeDeclaration.declarationSourceStart = source.sourceStart;
		typeDeclaration.declarationSourceEnd = source.sourceEnd;
		typeDeclaration.methods = new AbstractMethodDeclaration[] {
				createGetReferencedKeyMethod(source, relatedFieldName, isOneToOne, baseType, referenceType, compResult),
				createSetReferencedObjectMethod(fieldDecl, source, relatedFieldName, isOneToOne, isUnique, baseType, referenceType, (CompilationUnitDeclaration) fieldNode.top().get()),
				createSetRelatedIdMethod(source, relatedFieldName, isOneToOne, baseType, referenceType, compResult)
		};

		typeDeclaration.addClinit();
		
		QualifiedAllocationExpression allocation = new QualifiedAllocationExpression(typeDeclaration);
		setGeneratedBy(allocation, source);
		allocation.sourceStart = pS; allocation.sourceEnd = allocation.statementEnd = pE;
		allocation.type = fieldType;
		
		result = new FieldDeclaration(toProperCase(new String(fieldDecl.name)).toCharArray(), 0, -1);
		setGeneratedBy(result, source);
		result.declarationSourceEnd = -1;
		result.type = fieldType;
		result.initialization = allocation;
		result.modifiers = ClassFileConstants.AccPrivate | ClassFileConstants.AccFinal | ClassFileConstants.AccStatic;
		
		return result;
	}

	private static MethodDeclaration createGetReferencedKeyMethod(ASTNode source, String relatedFieldName, boolean isOneToOne, TypeReference baseType, TypeReference referenceType, CompilationResult compResult) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		MethodDeclaration getReferencedKey = new MethodDeclaration(compResult);
		setGeneratedBy(getReferencedKey, source);		
		getReferencedKey.modifiers = ClassFileConstants.AccPublic;
		getReferencedKey.annotations = new Annotation[] { makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, source) };
		getReferencedKey.returnType = createTypeReference(Long.class, p);
		setGeneratedBy(getReferencedKey.returnType, source);
		getReferencedKey.returnType.sourceStart = pS; getReferencedKey.returnType.sourceEnd = pE;
		getReferencedKey.arguments = new Argument[] {
				new Argument("item".toCharArray(), 0, (isOneToOne ? baseType : referenceType), ClassFileConstants.AccFinal)
		};
		setGeneratedBy(getReferencedKey.arguments[0], source);
		getReferencedKey.arguments[0].sourceStart = pS; getReferencedKey.arguments[0].sourceEnd = pE;
		getReferencedKey.selector = "getReferencedKey".toCharArray();
		getReferencedKey.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		getReferencedKey.bodyStart = getReferencedKey.declarationSourceStart = getReferencedKey.sourceStart = source.sourceStart;
		getReferencedKey.bodyEnd = getReferencedKey.declarationSourceEnd = getReferencedKey.sourceEnd = source.sourceEnd;
		
		MessageSend getRelatedFieldName = new MessageSend();
		setGeneratedBy(getRelatedFieldName, source);
		getRelatedFieldName.sourceStart = pS; getRelatedFieldName.sourceEnd = pE;
		getRelatedFieldName.receiver = new SingleNameReference("item".toCharArray(), p);
		setGeneratedBy(getRelatedFieldName.receiver, source);
		getRelatedFieldName.receiver.sourceStart = pS; getRelatedFieldName.receiver.sourceEnd = pE;
		getRelatedFieldName.selector = ("get" + toProperCase(relatedFieldName)).toCharArray();
		
		ReturnStatement returnStatement = new ReturnStatement(getRelatedFieldName, pS, pE);
		setGeneratedBy(returnStatement, source);
		
		getReferencedKey.statements = new Statement[] { returnStatement };
		
		return getReferencedKey;
	}
	
	private static MethodDeclaration createSetReferencedObjectMethod(FieldDeclaration fieldDecl, ASTNode source, String relatedFieldName, boolean isOneToOne, boolean isUnique, TypeReference baseType, TypeReference referenceType, CompilationUnitDeclaration compilationUnitDeclaration) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		TypeReference refType = fieldDecl.type;
		
		if (isUnique) {
			refType = new ParameterizedQualifiedTypeReference(Eclipse.fromQualifiedName(List.class.getName()), new TypeReference[][] {null, null, new TypeReference[] { refType } }, 0, new long[] { p, p, p });
			setGeneratedBy(refType, source);
			refType.sourceStart = pS; refType.sourceEnd = pE;
		}
		
		MethodDeclaration setReferencedObject = new MethodDeclaration(compilationUnitDeclaration.compilationResult);
		setGeneratedBy(setReferencedObject, source);		
		setReferencedObject.modifiers = ClassFileConstants.AccPublic;
		setReferencedObject.annotations = new Annotation[] { makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, source) };
		setReferencedObject.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
		setReferencedObject.returnType.sourceStart = pS; setReferencedObject.returnType.sourceEnd = pE;
		setReferencedObject.arguments = new Argument[] {
				new Argument("item".toCharArray(), 0, baseType, ClassFileConstants.AccFinal),
				new Argument("related".toCharArray(), 0, refType, ClassFileConstants.AccFinal),
		};
		setGeneratedBy(setReferencedObject.arguments[0], source);
		setGeneratedBy(setReferencedObject.arguments[1], source);
		setReferencedObject.arguments[0].sourceStart = pS; setReferencedObject.arguments[0].sourceEnd = pE;
		setReferencedObject.arguments[1].sourceStart = pS; setReferencedObject.arguments[1].sourceEnd = pE;
		setReferencedObject.selector = "setReferencedObject".toCharArray();
		setReferencedObject.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		setReferencedObject.bodyStart = setReferencedObject.declarationSourceStart = setReferencedObject.sourceStart = source.sourceStart;
		setReferencedObject.bodyEnd = setReferencedObject.declarationSourceEnd = setReferencedObject.sourceEnd = source.sourceEnd;
		
		List<Statement> statements = new ArrayList<Statement>();

		SingleNameReference fieldNameRef = new SingleNameReference("related".toCharArray(), p);
		setGeneratedBy(fieldNameRef, source);
		Expression fieldRef = createFieldAccessor(new String(fieldDecl.name), source, "item".toCharArray());
		setGeneratedBy(fieldRef, source);
		fieldRef.sourceStart = pS; fieldRef.sourceEnd = pE;
		
		if (isUnique) {
			MessageSend get = new MessageSend();
			setGeneratedBy(get, source);
			get.sourceStart = pS; get.sourceEnd = get.statementEnd = pE;
			get.receiver = new ThisReference(pS, pE);
			get.selector = "firstOrDefault".toCharArray();
			get.arguments = new Expression[] {
					fieldNameRef
			};
			
			Assignment assignment = new Assignment(fieldRef, get, pE);
			setGeneratedBy(assignment, source);
			assignment.sourceStart = pS; assignment.sourceEnd = assignment.statementEnd = pE;
			
			statements.add(assignment);
		} else {
			Assignment assignment = new Assignment(fieldRef, fieldNameRef, pE);
			assignment.sourceStart = pS; assignment.sourceEnd = assignment.statementEnd = pE;
			setGeneratedBy(assignment, source);
			statements.add(assignment);
		}
		
		setReferencedObject.statements = statements.toArray(new Statement[statements.size()]);
				
		return setReferencedObject;
	}
	
	private static MethodDeclaration createSetRelatedIdMethod(ASTNode source, String relatedFieldName, boolean isOneToOne, TypeReference baseType, TypeReference referenceType, CompilationResult compResult) {
		int pS = source.sourceStart, pE = source.sourceEnd;
		long p = (long)pS << 32 | pE;
		
		MethodDeclaration setRelatedId = new MethodDeclaration(compResult);
		setGeneratedBy(setRelatedId, source);
		setRelatedId.modifiers = ClassFileConstants.AccPublic;
		setRelatedId.annotations = new Annotation[] { makeMarkerAnnotation(TypeConstants.JAVA_LANG_OVERRIDE, source) };
		setRelatedId.returnType = TypeReference.baseTypeReference(TypeIds.T_void, 0);
		setRelatedId.returnType.sourceStart = pS; setRelatedId.returnType.sourceEnd = pE;
		
		TypeReference longRef = createTypeReference(Long.class, p);
		setGeneratedBy(longRef, source);
		longRef.sourceStart = pS; longRef.sourceEnd = pE;
		setRelatedId.arguments = new Argument[] {
				new Argument("item".toCharArray(), 0, (isOneToOne ? baseType : referenceType), ClassFileConstants.AccFinal),
				new Argument("id".toCharArray(), 0, longRef, ClassFileConstants.AccFinal)
		};
		setGeneratedBy(setRelatedId.arguments[0], source);
		setGeneratedBy(setRelatedId.arguments[1], source);
		setRelatedId.arguments[0].sourceStart = pS; setRelatedId.arguments[0].sourceEnd = pE;
		setRelatedId.arguments[1].sourceStart = pS; setRelatedId.arguments[1].sourceEnd = pE;
		setRelatedId.selector = "setRelatedId".toCharArray();
		setRelatedId.bits |= ECLIPSE_DO_NOT_TOUCH_FLAG;
		setRelatedId.bodyStart = setRelatedId.declarationSourceStart = setRelatedId.sourceStart = source.sourceStart;
		setRelatedId.bodyEnd = setRelatedId.declarationSourceEnd = setRelatedId.sourceEnd = source.sourceEnd;
		
		MessageSend setRelatedIdName = new MessageSend();
		setGeneratedBy(setRelatedIdName, source);
		setRelatedIdName.sourceStart = pS; setRelatedIdName.sourceEnd = setRelatedIdName.statementEnd = pE;
		setRelatedIdName.receiver = new SingleNameReference("item".toCharArray(), p);
		setGeneratedBy(setRelatedIdName.receiver, source);
		setRelatedIdName.receiver.sourceStart = pS; setRelatedIdName.receiver.sourceEnd = pE;
		setRelatedIdName.selector = ("set" + toProperCase(relatedFieldName)).toCharArray();
		setRelatedIdName.arguments = new Expression[] {
				new SingleNameReference("id".toCharArray(), p)
		};
		setGeneratedBy(setRelatedIdName.arguments[0], source);
		setRelatedIdName.arguments[0].sourceStart = pS; setRelatedIdName.arguments[0].sourceEnd = pE;
		
		setRelatedId.statements = new Statement[] { setRelatedIdName };
		
		return setRelatedId;
	}
	
	private static String toProperCase(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}
	
	private static TypeReference createTypeReference(Class<?> clazz, long p) {
		return createTypeReference(clazz.getName().split("\\."), p);	
	}
	
	private static TypeReference createTypeReference(String[] parts, long p) {
		if (parts.length == 1) return new SingleTypeReference(parts[0].toCharArray(), p);
		
		long[] ps = new long[parts.length];
		char[][] tokens = new char[parts.length][];
		for (int i = 0; i < parts.length; i++) {
			ps[i] = p;
			tokens[i] = parts[i].toCharArray();
		}
		
		return new QualifiedTypeReference(tokens, ps);
	}
}
