Archaeological datasets
==============

Selection of archaeological dataset is based on `<ddm:audience>D37000<ddm:audience>`

As far as recognised, fields are changed into controlled vocabularies with attributes
`valueURI`, `subjectScheme` and `schemURI`.

`<...:temporal xsi:type="abr:ABR...">` becomes `<ddm:temporal>`  
`<...:subject xsi:type="abr:ABR...">` becomes `<ddm:subject>`

Titles may get replaced with `<ddm:reportNumber>` or `<ddm:acquisitionMethod>`
The content of these tags is preserved.
When a title starts with a report number, the original title is entirely preserved.
A title in `<ddm:profile>` is always preserved but they may lead to
`<ddm:reportNumber>` or ``<ddm:acquisitionMethod>`` at the start of `<ddm:dcmiMetadata>`
