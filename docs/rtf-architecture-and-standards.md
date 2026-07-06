# RTF Architecture and Standards Notes

This project is a general-purpose RTF parser and HTML converter for Java. Outlook/Exchange RTF body
conversion is a first-class extension because that is the compatibility target needed by
`outlook-message-parser`.

## References

- [Rich Text Format (RTF) Specification Version 1.9.1](https://officeprotocoldoc.z19.web.core.windows.net/files/Archive_References/%5BMSFT-RTF%5D.pdf)
- [MS-OXRTFEX overview](https://learn.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxrtfex/411d0d58-49f7-496c-b8c3-5859b045f6cf)
- [MS-OXRTFEX: Encoding HTML into RTF](https://learn.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxrtfex/4f09a809-9910-43f3-a67c-3506b09ca5ac)
- [MS-OXRTFEX: Extracting Encapsulated HTML from RTF](https://learn.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxrtfex/906fbb0f-2467-490e-8c3e-bdc31c5e9d35)
- [MS-OXRTFEX: RTF Content](https://learn.microsoft.com/en-us/openspecs/exchange_server_protocols/ms-oxrtfex/e81417a4-2ec8-4685-9004-cb8a10e7ca1f)
- [IANA media type registry](https://www.iana.org/assignments/media-types/media-types.xhtml), including `text/rtf`

The standards are linked rather than copied into the repository.

## Parser Contract

`RtfParser` parses RTF into a source-preserving document model:

- `RtfDocument` owns the synthetic root group, original source text, and whether the source came from
  bytes.
- `RtfGroup` scopes child nodes and formatting state.
- `RtfControlWord` represents control words such as `\ansi`, `\ansicpg1252`, `\u945`, and `\par`.
- `RtfControlSymbol` represents escaped symbols such as `\*`, `\{`, `\}`, `\\`, `\~`, and `\-`.
- `RtfText` represents literal text runs.
- `RtfHexBytes` represents escaped byte runs such as `\'82\'b1`.
- `RtfBinary` represents `\binN` payloads.
- `RtfPosition` stores character offsets back into the original RTF source.

The parser follows the RTF model of groups, control words, control symbols, and text. Formatting state
is applied by the renderer and scoped by groups.

## Standard Rendering

`StandardRtfToHtmlConverter` renders conservative HTML for normal RTF documents. Current coverage
includes paragraph breaks, line breaks, tabs, bold, italic, underline, strikethrough, paragraph
alignment, font size, Unicode escapes, charset-aware byte escapes, and optional `\pict` image
extraction through `RtfImageHandler`.

Important RTF rules used by the renderer:

- Control-word separators are not visible text.
- Source line endings are ignored; visible line breaks come from controls such as `\par` and `\line`.
- Unknown ignorable destinations, written as groups beginning with `\*`, are skipped unless explicitly
  supported.
- `\ansicpgN` selects the default ANSI code page.
- Font-table entries can override the default code page with `\fcharsetN`; `\cpgN` has higher
  priority than `\fcharsetN`.
- Escaped byte runs are decoded with the current font's effective charset.
- `\uN` emits a Unicode code unit and skips the following `\ucN` fallback characters.
- `\binN` payloads are skipped unless they belong to a supported binary destination such as `\pict`.

## Outlook Extension

`OutlookRtfToHtmlConverter` uses the same parser and renderer core, then applies MS-OXRTFEX behavior
when Outlook markers are present:

- `\fromhtmlN`: original HTML is embedded in RTF.
- `\fromtext`: original plain text is embedded in RTF.

For `\fromhtmlN`, the Outlook converter extracts the original HTML:

- `\htmltagN` groups are copied as original HTML markup.
- `\htmlrtf` suppressed content is skipped until `\htmlrtf0`.
- Visible text outside `\htmltagN` is decoded with the current font code page.
- `\par` and `\line` become line breaks, and `\tab` becomes a tab.
- Non-visible destinations such as font tables, color tables, bookmarks, generator metadata, and list
  text are skipped.

For `\fromtext`, the converter extracts visible text, escapes it, and returns HTML using a
`<pre style="white-space:pre-wrap">` wrapper. That keeps Outlook plain-text email layout intact.

If neither Outlook marker is present, `OutlookRtfToHtmlConverter` falls back to the standard renderer.

## Legacy Converters

Two older converters remain under `org.bbottema.rtftohtml.legacy` for comparison:

- `ClassicRtfToHtmlConverter`: the inherited regex-based converter.
- `JEditorPaneRtfToHtmlConverter`: Swing's built-in limited RTF reader.

They are kept as historical reference implementations, not as the recommended conversion path.

## Issue Coverage

- #2: Outlook metadata is skipped; `\ansicpg`, `\fcharset`, and `\line` are handled.
- #3: `\strike` renders as `text-decoration:line-through`.
- #4: `\qc` and `\qr` render paragraph alignment in generic RTF.
- #5: `\pict` groups are parsed and exposed to an image callback.
- #6: Outlook `\fromtext` returns safe HTML preserving plain-text line breaks.
- #7: `\line` is preserved during Outlook HTML extraction.
- #9 and #13: charset detection uses `\ansicpg` plus font-table overrides.
- #14: `\pntext` is skipped so browser-rendered lists do not get duplicate numbers.
