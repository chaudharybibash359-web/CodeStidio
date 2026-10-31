package com.example.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.ui.theme.VsCodeComment
import com.example.ui.theme.VsCodeFunction
import com.example.ui.theme.VsCodeKeyword
import com.example.ui.theme.VsCodeNumber
import com.example.ui.theme.VsCodeProperty
import com.example.ui.theme.VsCodeString
import com.example.ui.theme.VsCodeTag
import com.example.ui.theme.VsCodeTextPrimary
import java.util.regex.Pattern

class CodeSyntaxVisualTransformation(
    private val fileType: String
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val builder = AnnotatedString.Builder(raw)

        // Default style
        builder.addStyle(
            SpanStyle(color = VsCodeTextPrimary, fontFamily = FontFamily.Monospace),
            0,
            raw.length
        )

        try {
            when (fileType.uppercase()) {
                "HTML", "HTM" -> highlightHtml(builder, raw)
                "CSS" -> highlightCss(builder, raw)
                "JS", "JAVASCRIPT", "JSON" -> highlightJs(builder, raw)
                else -> highlightGeneric(builder, raw)
            }
        } catch (_: Exception) {
            // Fail safely to plain text
        }

        return TransformedText(builder.toAnnotatedString(), OffsetMapping.Identity)
    }

    private fun highlightHtml(builder: AnnotatedString.Builder, text: String) {
        // Tag brackets and names: <div, </div, <span>
        applyRegex(builder, text, Pattern.compile("</?[a-zA-Z0-9_-]+>?"), VsCodeTag, FontWeight.SemiBold)
        // Attributes: class=, id=, src=
        applyRegex(builder, text, Pattern.compile("\\b[a-zA-Z0-9_-]+(?=\\=)"), VsCodeProperty)
        // Attribute strings: "value" or 'value'
        applyRegex(builder, text, Pattern.compile("\"[^\"]*\"|'[^']*'"), VsCodeString)
        // Comments: <!-- comment -->
        applyRegex(builder, text, Pattern.compile("<!--[\\s\\S]*?-->"), VsCodeComment)
    }

    private fun highlightCss(builder: AnnotatedString.Builder, text: String) {
        // Properties: color:, background:, etc.
        applyRegex(builder, text, Pattern.compile("\\b[a-zA-Z0-9_-]+(?=\\:)"), VsCodeProperty)
        // Numbers and units: 10px, 1.5rem, #fff
        applyRegex(builder, text, Pattern.compile("#[0-9a-fA-F]{3,8}|\\b\\d+(\\.\\d+)?(px|rem|em|%|vh|vw|s|ms)?\\b"), VsCodeNumber)
        // Selectors
        applyRegex(builder, text, Pattern.compile("[.#][a-zA-Z0-9_-]+"), VsCodeFunction, FontWeight.Medium)
        // Strings
        applyRegex(builder, text, Pattern.compile("\"[^\"]*\"|'[^']*'"), VsCodeString)
        // Comments: /* comment */
        applyRegex(builder, text, Pattern.compile("/\\*[\\s\\S]*?\\*/"), VsCodeComment)
    }

    private fun highlightJs(builder: AnnotatedString.Builder, text: String) {
        // JS Keywords
        val keywords = "\\b(const|let|var|function|return|if|else|for|while|do|switch|case|break|default|continue|class|new|import|export|from|as|async|await|try|catch|finally|throw|typeof|instanceof|this|super|null|undefined|true|false)\\b"
        applyRegex(builder, text, Pattern.compile(keywords), VsCodeKeyword, FontWeight.SemiBold)

        // Strings: "...", '...', `...`
        applyRegex(builder, text, Pattern.compile("\"[^\"]*\"|'[^']*'|`[^`]*`"), VsCodeString)

        // Numbers
        applyRegex(builder, text, Pattern.compile("\\b\\d+(\\.\\d+)?\\b"), VsCodeNumber)

        // Function calls: foo(...)
        applyRegex(builder, text, Pattern.compile("\\b[a-zA-Z0-9_]+(?=\\()"), VsCodeFunction)

        // Line comments: // ...
        applyRegex(builder, text, Pattern.compile("//.*"), VsCodeComment)

        // Block comments: /* ... */
        applyRegex(builder, text, Pattern.compile("/\\*[\\s\\S]*?\\*/"), VsCodeComment)
    }

    private fun highlightGeneric(builder: AnnotatedString.Builder, text: String) {
        applyRegex(builder, text, Pattern.compile("\"[^\"]*\"|'[^']*'"), VsCodeString)
        applyRegex(builder, text, Pattern.compile("\\b\\d+\\b"), VsCodeNumber)
    }

    private fun applyRegex(
        builder: AnnotatedString.Builder,
        text: String,
        pattern: Pattern,
        color: Color,
        weight: FontWeight = FontWeight.Normal
    ) {
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            if (start in text.indices && end <= text.length) {
                builder.addStyle(SpanStyle(color = color, fontWeight = weight), start, end)
            }
        }
    }
}
