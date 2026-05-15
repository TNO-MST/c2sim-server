package org.c2sim.lox;

import jakarta.xml.bind.annotation.XmlSchema;
import org.c2sim.lox.schema.MessageType;

public class LoxSchemaInfo {

    // Prevent instantiation
    private LoxSchemaInfo() {
        throw new AssertionError("Only static functions");
    }


    /**
     * Returns the XML namespace used by the JAXB-generated C2SIM schema classes.
     *
     * <p>The namespace is obtained dynamically from the package-level
     * {@link javax.xml.bind.annotation.XmlSchema} annotation that is generated
     * by JAXB during code generation from the XML Schema (XSD).</p>
     *
     * <p>This avoids hardcoding the namespace value in application code and
     * ensures the namespace always matches the currently generated JAXB model.</p>
     *
     * @return the JAXB schema namespace, {@code "<unknown>"} if no namespace
     *         annotation is present, or {@code "<error>"} if retrieval fails
     */
    public static String getC2SimNamespace() {
        try {
            XmlSchema schema = MessageType.class.getPackage().getAnnotation(XmlSchema.class);
            return schema != null ? schema.namespace() : "<unknown>";
        } catch (Exception ex) {
            return "<error>";
        }
    }

}

