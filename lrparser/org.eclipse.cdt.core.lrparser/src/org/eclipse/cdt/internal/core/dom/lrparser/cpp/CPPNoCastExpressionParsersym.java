/*******************************************************************************
* Copyright (c) 2006, 2011 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*     IBM Corporation - initial API and implementation
*********************************************************************************/

// This file was generated by LPG
package org.eclipse.cdt.internal.core.dom.lrparser.cpp;

public interface CPPNoCastExpressionParsersym {
    public final static int
      TK_asm = 65,
      TK_auto = 25,
      TK_bool = 11,
      TK_break = 76,
      TK_case = 77,
      TK_catch = 113,
      TK_char = 12,
      TK_class = 55,
      TK_const = 23,
      TK_const_cast = 41,
      TK_continue = 78,
      TK_default = 79,
      TK_delete = 66,
      TK_do = 80,
      TK_double = 13,
      TK_dynamic_cast = 42,
      TK_else = 121,
      TK_enum = 56,
      TK_explicit = 26,
      TK_export = 88,
      TK_extern = 27,
      TK_false = 43,
      TK_float = 14,
      TK_for = 81,
      TK_friend = 28,
      TK_goto = 82,
      TK_if = 83,
      TK_inline = 29,
      TK_int = 15,
      TK_long = 16,
      TK_mutable = 30,
      TK_namespace = 61,
      TK_new = 67,
      TK_operator = 8,
      TK_private = 114,
      TK_protected = 115,
      TK_public = 116,
      TK_register = 31,
      TK_reinterpret_cast = 44,
      TK_return = 84,
      TK_short = 17,
      TK_signed = 18,
      TK_sizeof = 45,
      TK_static = 32,
      TK_static_cast = 46,
      TK_struct = 57,
      TK_switch = 85,
      TK_template = 53,
      TK_this = 47,
      TK_throw = 59,
      TK_try = 74,
      TK_true = 48,
      TK_typedef = 33,
      TK_typeid = 49,
      TK_typename = 10,
      TK_union = 58,
      TK_unsigned = 19,
      TK_using = 63,
      TK_virtual = 22,
      TK_void = 20,
      TK_volatile = 24,
      TK_wchar_t = 21,
      TK_while = 75,
      TK_integer = 50,
      TK_floating = 51,
      TK_charconst = 52,
      TK_stringlit = 38,
      TK_identifier = 1,
      TK_Completion = 2,
      TK_EndOfCompletion = 9,
      TK_Invalid = 122,
      TK_LeftBracket = 60,
      TK_LeftParen = 3,
      TK_Dot = 119,
      TK_DotStar = 98,
      TK_Arrow = 117,
      TK_ArrowStar = 91,
      TK_PlusPlus = 36,
      TK_MinusMinus = 37,
      TK_And = 7,
      TK_Star = 5,
      TK_Plus = 34,
      TK_Minus = 35,
      TK_Tilde = 6,
      TK_Bang = 39,
      TK_Slash = 99,
      TK_Percent = 100,
      TK_RightShift = 86,
      TK_LeftShift = 87,
      TK_LT = 54,
      TK_GT = 64,
      TK_LE = 89,
      TK_GE = 90,
      TK_EQ = 92,
      TK_NE = 93,
      TK_Caret = 94,
      TK_Or = 95,
      TK_AndAnd = 96,
      TK_OrOr = 101,
      TK_Question = 102,
      TK_Colon = 71,
      TK_ColonColon = 4,
      TK_DotDotDot = 97,
      TK_Assign = 70,
      TK_StarAssign = 103,
      TK_SlashAssign = 104,
      TK_PercentAssign = 105,
      TK_PlusAssign = 106,
      TK_MinusAssign = 107,
      TK_RightShiftAssign = 108,
      TK_LeftShiftAssign = 109,
      TK_AndAssign = 110,
      TK_CaretAssign = 111,
      TK_OrAssign = 112,
      TK_Comma = 68,
      TK_RightBracket = 118,
      TK_RightParen = 72,
      TK_RightBrace = 73,
      TK_SemiColon = 40,
      TK_LeftBrace = 69,
      TK_ERROR_TOKEN = 62,
      TK_EOF_TOKEN = 120;

      public final static String orderedTerminalSymbols[] = {
                 "",
                 "identifier",
                 "Completion",
                 "LeftParen",
                 "ColonColon",
                 "Star",
                 "Tilde",
                 "And",
                 "operator",
                 "EndOfCompletion",
                 "typename",
                 "bool",
                 "char",
                 "double",
                 "float",
                 "int",
                 "long",
                 "short",
                 "signed",
                 "unsigned",
                 "void",
                 "wchar_t",
                 "virtual",
                 "const",
                 "volatile",
                 "auto",
                 "explicit",
                 "extern",
                 "friend",
                 "inline",
                 "mutable",
                 "register",
                 "static",
                 "typedef",
                 "Plus",
                 "Minus",
                 "PlusPlus",
                 "MinusMinus",
                 "stringlit",
                 "Bang",
                 "SemiColon",
                 "const_cast",
                 "dynamic_cast",
                 "false",
                 "reinterpret_cast",
                 "sizeof",
                 "static_cast",
                 "this",
                 "true",
                 "typeid",
                 "integer",
                 "floating",
                 "charconst",
                 "template",
                 "LT",
                 "class",
                 "enum",
                 "struct",
                 "union",
                 "throw",
                 "LeftBracket",
                 "namespace",
                 "ERROR_TOKEN",
                 "using",
                 "GT",
                 "asm",
                 "delete",
                 "new",
                 "Comma",
                 "LeftBrace",
                 "Assign",
                 "Colon",
                 "RightParen",
                 "RightBrace",
                 "try",
                 "while",
                 "break",
                 "case",
                 "continue",
                 "default",
                 "do",
                 "for",
                 "goto",
                 "if",
                 "return",
                 "switch",
                 "RightShift",
                 "LeftShift",
                 "export",
                 "LE",
                 "GE",
                 "ArrowStar",
                 "EQ",
                 "NE",
                 "Caret",
                 "Or",
                 "AndAnd",
                 "DotDotDot",
                 "DotStar",
                 "Slash",
                 "Percent",
                 "OrOr",
                 "Question",
                 "StarAssign",
                 "SlashAssign",
                 "PercentAssign",
                 "PlusAssign",
                 "MinusAssign",
                 "RightShiftAssign",
                 "LeftShiftAssign",
                 "AndAssign",
                 "CaretAssign",
                 "OrAssign",
                 "catch",
                 "private",
                 "protected",
                 "public",
                 "Arrow",
                 "RightBracket",
                 "Dot",
                 "EOF_TOKEN",
                 "else",
                 "Invalid"
             };

    public final static boolean isValidForParser = true;
}
