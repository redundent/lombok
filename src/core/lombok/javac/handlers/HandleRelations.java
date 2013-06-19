/*
 * Copyright (C) 2009-2012 The Project Lombok Authors.
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
package lombok.javac.handlers;

import static lombok.javac.Javac.getCtcInt;
import static lombok.javac.handlers.JavacHandlerUtil.*;

import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import lombok.OneToMany;
import lombok.OneToOne;
import lombok.core.AST.Kind;
import lombok.core.AnnotationValues;
import lombok.core.data.OneToManyRelation;
import lombok.core.data.OneToOneRelation;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.JavacHandlerUtil.FieldAccess;
import lombok.javac.handlers.JavacHandlerUtil.MemberExistsResult;

import org.mangosdk.spi.ProviderFor;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeParameter;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

/**
 * Handles the {@code lombok.OneToOne} and {@code lombok.OneToMany} annotation for javac.
 */
public class HandleRelations {
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleOneToOne extends JavacAnnotationHandler<OneToOne> {
		@Override
		public void handle(AnnotationValues<OneToOne> annotation, JCAnnotation ast, JavacNode annotationNode) {
			HandleRelations.handle(annotation, ast, annotationNode);
		}
	}
	
	@ProviderFor(JavacAnnotationHandler.class)
	public static class HandleOneToMany extends JavacAnnotationHandler<OneToMany> {
		@Override
		public void handle(AnnotationValues<OneToMany> annotation, JCAnnotation ast, JavacNode annotationNode) {
			HandleRelations.handle(annotation, ast, annotationNode);
		}
	}
	
	private static void handle(AnnotationValues<?> annotation, JCAnnotation ast, JavacNode annotationNode) {
		if (inNetbeansEditor(annotationNode)) return;
		
		Object anno = annotation.getInstance();
		
		JavacNode fieldNode = annotationNode.up();
		
		if (fieldNode == null || fieldNode.getKind() != Kind.FIELD || !(fieldNode.get() instanceof JCVariableDecl)) {
			annotationNode.addError("@OneToOne is legal only on fields.");				
			return;
		}
		
		JCVariableDecl field = (JCVariableDecl)fieldNode.get();
		
		if ((field.mods.flags & Flags.STATIC) != 0) {
			annotationNode.addError("@OneToOne is not legal on static fields.");
			return;
		}
		
		if (fieldExists(toUpperCase(field.name.toString()), fieldNode) == MemberExistsResult.NOT_EXISTS) {
			JCVariableDecl fieldDecl = createField(anno, fieldNode);
			injectFieldSuppressWarnings(fieldNode.up(), fieldDecl);
		}
	}
	
	private static JCVariableDecl createField(Object anno, JavacNode fieldNode) {
		TreeMaker maker = fieldNode.getTreeMaker();
		JCVariableDecl field = (JCVariableDecl) fieldNode.get();
		
		String relatedFieldName = null;
		boolean isOneToOne = false;
		boolean isUnique = false;

		Name baseTypeName = ((JCClassDecl) fieldNode.up().get()).name;
		JCIdent baseType = maker.Ident(baseTypeName);		
		JCExpression referenceType = getFieldType(fieldNode, FieldAccess.ALWAYS_FIELD);
		
		if (anno instanceof OneToOne) {
			isOneToOne = true;
			relatedFieldName = ((OneToOne) anno).field();
		} else {
			relatedFieldName = ((OneToMany) anno).field();
			isUnique = ((OneToMany) anno).unique();

			if (referenceType instanceof JCTypeApply) {
				referenceType = ((JCTypeApply) referenceType).arguments.get(0);
			}
		}
		
		JCClassDecl anonClass = maker.AnonymousClassDef(
				maker.Modifiers(0),
				List.<JCTree>of(
						createGetReferencedKeyMethod(fieldNode, maker, relatedFieldName, isOneToOne, baseType, referenceType),
						createSetReferencedObjectMethod(fieldNode, maker, field, baseType, isUnique),
						createSetRelatedIdMethod(fieldNode, maker, relatedFieldName, isOneToOne, baseType, referenceType)
				)
		);
		
		JCVariableDecl var = maker.VarDef(
				maker.Modifiers(Flags.PUBLIC | Flags.STATIC | Flags.FINAL),
				fieldNode.toName(toUpperCase(field.name.toString())),
				maker.TypeApply(
						(isOneToOne ? chainDots(fieldNode, OneToOneRelation.class) : chainDots(fieldNode, OneToManyRelation.class)),
						List.<JCExpression>of(baseType, referenceType)
				),
				maker.NewClass(
						null,
						List.<JCExpression>nil(),
						maker.TypeApply(
								(isOneToOne ? chainDots(fieldNode, OneToOneRelation.class) : chainDots(fieldNode, OneToManyRelation.class)),
								List.<JCExpression>of(baseType, referenceType)
						),
						List.<JCExpression>nil(),
						anonClass
				)
		);
		
		return var;
	}

	private static JCMethodDecl createSetRelatedIdMethod(JavacNode fieldNode, TreeMaker maker, String relatedFieldName, boolean isOneToOne, JCIdent baseType, JCExpression referenceType) {
		return maker.MethodDef(
				maker.Modifiers(Flags.PUBLIC, List.of(maker.Annotation(chainDots(fieldNode, Override.class), List.<JCExpression>nil()))),
				fieldNode.toName("setRelatedId"),
				maker.Type(new JCNoType(getCtcInt(TypeTags.class, "VOID"))),
				List.<JCTypeParameter>nil(),
				List.<JCVariableDecl>of(
						maker.VarDef(
								maker.Modifiers(Flags.FINAL),
								fieldNode.toName("item"),
								(isOneToOne ? baseType : referenceType),
								null
						),
						maker.VarDef(
								maker.Modifiers(Flags.FINAL),
								fieldNode.toName("id"),
								chainDots(fieldNode, Long.class),
								null
						)
				),
				List.<JCExpression>nil(),
				maker.Block(
						0,
						List.<JCStatement>of(
								maker.Exec(
										maker.Apply(
												List.<JCExpression>nil(),
												maker.Select(
													maker.Ident(fieldNode.toName("item")),
													fieldNode.toName("set" + toProperCase(relatedFieldName))
												),
												List.<JCExpression>of(
														maker.Ident(fieldNode.toName("id"))
												)
										)
								)
						)
				),
				null
		);
	}

	private static JCMethodDecl createSetReferencedObjectMethod(JavacNode fieldNode, TreeMaker maker, JCVariableDecl field, JCIdent baseType, boolean isUnique) {
		JCExpression refVariableType = getFieldType(fieldNode, FieldAccess.ALWAYS_FIELD);
		if (isUnique) {
			refVariableType = maker.TypeApply(
					chainDots(fieldNode, java.util.List.class),
					List.<JCExpression>of(refVariableType)
			);
		}
		
		JCStatement statement = null;
		if (isUnique) {
			statement = maker.If(
					maker.Binary(
							getCtcInt(JCTree.class, "GT"),
							maker.Apply(
									List.<JCExpression>nil(),
									maker.Select(
											maker.Ident(fieldNode.toName("ref")),
											fieldNode.toName("size")
									),
									List.<JCExpression>nil()
							),
							maker.Literal(getCtcInt(TypeTags.class, "INT"), 0)
					),
					maker.Exec(
							maker.Assign(
									maker.Select(
											maker.Ident(fieldNode.toName("item")),
											field.name
									),
									maker.Apply(
											List.<JCExpression>nil(),
											maker.Select(
													maker.Ident(fieldNode.toName("ref")),
													fieldNode.toName("get")						
											),
											List.<JCExpression>of(
													maker.Literal(getCtcInt(TypeTags.class, "INT"), 0)
											)
									)
							)
					),
					null
			);
		} else {
			statement = maker.Exec(
					maker.Assign(
							maker.Select(
								maker.Ident(fieldNode.toName("item")),
								field.name
							),
							maker.Ident(fieldNode.toName("ref"))
					)
			);
		}
		return maker.MethodDef(
				maker.Modifiers(Flags.PUBLIC, List.of(maker.Annotation(chainDots(fieldNode, Override.class), List.<JCExpression>nil()))),
				fieldNode.toName("setReferencedObject"),
				maker.Type(new JCNoType(getCtcInt(TypeTags.class, "VOID"))),
				List.<JCTypeParameter>nil(),
				List.<JCVariableDecl>of(
						maker.VarDef(
								maker.Modifiers(Flags.FINAL),
								fieldNode.toName("item"),
								baseType,
								null
						),
						maker.VarDef(
								maker.Modifiers(Flags.FINAL),
								fieldNode.toName("ref"),
								refVariableType,
								null
						)
				),
				List.<JCExpression>nil(),
				maker.Block(
						0,
						List.<JCStatement>of(
								statement
						)
				),
				null
		);
	}

	private static JCMethodDecl createGetReferencedKeyMethod(JavacNode fieldNode, TreeMaker maker, String relatedFieldName, boolean isOneToOne, JCIdent baseType, JCExpression referenceType) {
		return maker.MethodDef(
				maker.Modifiers(Flags.PUBLIC, List.of(maker.Annotation(chainDots(fieldNode, Override.class), List.<JCExpression>nil()))),
				fieldNode.toName("getReferencedKey"),
				chainDots(fieldNode, Long.class),
				List.<JCTypeParameter>nil(),
				List.<JCVariableDecl>of(
						maker.VarDef(
								maker.Modifiers(Flags.FINAL),
								fieldNode.toName("item"),
								(isOneToOne ? baseType : referenceType),
								null
						)
				),
				List.<JCExpression>nil(),
				maker.Block(
						0,
						List.<JCStatement>of(
								maker.Return(
										maker.Apply(
												List.<JCExpression>nil(),
												maker.Select(
													maker.Ident(fieldNode.toName("item")),
													fieldNode.toName("get" + toProperCase(relatedFieldName))
												),
												List.<JCExpression>nil()
										)
								)
						)
				),
				null
		);
	}

	private static String toUpperCase(String name) {
		StringBuilder sb = new StringBuilder();
		for (char c : name.toCharArray()) {
			if (Character.isUpperCase(c)) {
				sb.append("_");
			}
			
			sb.append(Character.toUpperCase(c));
		}
		
		return sb.toString();
	}
	
	private static String toProperCase(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	private static class JCNoType extends Type implements NoType {
		public JCNoType(int tag) {
			super(tag, null);
		}
		
		@Override
		public TypeKind getKind() {
			if (tag == getCtcInt(TypeTags.class, "VOID")) return TypeKind.VOID;
			if (tag == getCtcInt(TypeTags.class, "NONE")) return TypeKind.NONE;
			throw new AssertionError("Unexpected tag: " + tag);
		}
		
		@Override
		public <R, P> R accept(TypeVisitor<R, P> v, P p) {
			return v.visitNoType(this, p);
		}
	}
}
