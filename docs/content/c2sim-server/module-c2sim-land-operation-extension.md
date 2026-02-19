# Module C2SIM XML (LOX)

This module generates Java classes from the Lox/C2SIM XML Schema definitions (XSD).

The schema files are located in:

```
src/main/resources/lox/xsd/2025
```

To convert these XSD files into strongly typed Java classes, the project uses the **`jaxb2-maven-plugin`**.



Code generation can be triggered manually with:

```
mvn clean generate-sources
```

This approach guarantees that the Java model remains fully aligned with the authoritative XML schema definitions.



# Jakarta JAXB

**Jakarta XML Binding (JAXB)** is a framework for mapping XML schemas (XSD) to Java classes and vice versa.



## Helper function

The module also providers extra helper functions like:

* From `XML text` to `JAVA POJO`

* From `JAVA POJO` to `XML text`

* XSD validation of xml (xml text and `JAVA POJO`)

* Use builder pattern to create `C2SIM message`

* Detection of `C2SIM Message kind` in xml text (without paring complete xml)

* Searching for objects in XML

* .....



## Deserialize / serialize  C2SIM XML message

The class `org.c2sim.lox.helpers.MessageTypeHelper` can be used to serialize and deserialize `C2SIM messages`.

```
MessageBodyType msgAsObject = MessageTypeHelper.readMessage(xmlText);
String xmlAsText = MessageTypeHelper.writeMessageAsString(msg, true, true);
```

## Validate C2SIM XML message

The class `org.c2sim.lox.validation.LoxXsdValidator` can be used to validate `XML` against the `C2SIM XSD`. The class can return a simple `boolean` or an complete `error report` (with line numbers).

```
LoxXsdValidator.doValidation(xml).isValid()
```

## Determine C2SIM Message kind

The method `DetectMsgKind.determineMsgKindMeasured` can be used to get the `C2SIM Message kind` from an XML text. The method is optimized to not completely parse the XML message. 

```
var result = DetectMsgKind.determineMsgKindMeasured(xml);
The result is an enumeration of the type `org.c2sim.lox.C2SimMsgKind`.
```



## 








