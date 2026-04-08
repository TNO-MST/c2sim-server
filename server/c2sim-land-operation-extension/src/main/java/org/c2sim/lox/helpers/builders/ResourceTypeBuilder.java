package org.c2sim.lox.helpers.builders;

import java.math.BigInteger;
import org.c2sim.lox.schema.ResourceType;
import org.c2sim.lox.schema.SISOEntityTypeType;

/**
 * Builder for {@link ResourceType} objects.
 *
 * <p>{@code sisoEntityType} is required before calling {@link #build()}.
 */
public class ResourceTypeBuilder {
  private final ResourceType resource;

  private ResourceTypeBuilder() {
    this.resource = new ResourceType();
  }

  /**
   * Creates a new, empty builder.
   *
   * @return a new builder instance
   */
  public static ResourceTypeBuilder create() {
    return new ResourceTypeBuilder();
  }

  /**
   * Sets the resource name.
   *
   * @param name a human-readable name for the resource
   * @return this builder
   */
  public ResourceTypeBuilder name(String name) {
    resource.setName(name);
    return this;
  }

  /**
   * Sets the on-hand quantity.
   *
   * @param quantity the on-hand quantity, or {@code null}
   * @return this builder
   */
  public ResourceTypeBuilder onHandQuantity(Double quantity) {
    resource.setOnHandQuantity(quantity);
    return this;
  }

  /**
   * Sets the operational quantity.
   *
   * @param quantity the operational quantity
   * @return this builder
   */
  public ResourceTypeBuilder operationalQuantity(double quantity) {
    resource.setOperationalQuantity(quantity);
    return this;
  }

  /**
   * Sets the required on-hand quantity.
   *
   * @param quantity the required on-hand quantity, or {@code null}
   * @return this builder
   */
  public ResourceTypeBuilder requiredOnHandQuantity(Double quantity) {
    resource.setRequiredOnHandQuantity(quantity);
    return this;
  }

  /**
   * Sets the SISO / DIS entity type from a dot- or colon-separated string.
   *
   * <p>The string must contain exactly 7 integer components in the order: {@code
   * Kind.Domain.Country.Category.SubCategory.Specific.Extra}.
   *
   * @param entityType the dot/colon-separated DIS entity type string
   * @return this builder
   * @throws IllegalArgumentException if the string does not have exactly 7 components
   */
  // EntityKind.Domain.Country.Category.SubCategory.Specific.Extra
  public ResourceTypeBuilder sisoEntityType(String entityType) {
    String[] parts = entityType.split("[.:]");
    if (parts.length != 7) {
      throw new IllegalArgumentException(
          "Invalid DIS entity notation: must have 7 dot-separated integers");
    }

    SISOEntityTypeType entity = new SISOEntityTypeType();
    entity.setDISKind((byte) Integer.parseInt(parts[0]));
    entity.setDISDomain((byte) Integer.parseInt(parts[1]));
    entity.setDISCountry(new BigInteger(parts[2]));
    entity.setDISCategory((byte) Integer.parseInt(parts[3]));
    entity.setDISSubCategory((byte) Integer.parseInt(parts[4]));
    entity.setDISSpecific((byte) Integer.parseInt(parts[5]));
    entity.setDISExtra((byte) Integer.parseInt(parts[6]));
    sisoEntityType(entity);
    return this;
  }

  /**
   * Sets the SISO entity type directly.
   *
   * @param entityType the SISO entity type object
   * @return this builder
   */
  public ResourceTypeBuilder sisoEntityType(SISOEntityTypeType entityType) {
    resource.setSISOEntityType(entityType);
    return this;
  }

  /**
   * Builds and returns the {@link ResourceType}.
   *
   * @return the constructed resource
   * @throws IllegalArgumentException if {@code sisoEntityType} is not set
   */
  public ResourceType build() {
    if (resource.getSISOEntityType() == null) {
      throw new IllegalArgumentException("SISOEntityType is null");
    }
    return resource;
  }
}
