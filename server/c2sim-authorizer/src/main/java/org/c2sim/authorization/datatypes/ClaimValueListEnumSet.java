package org.c2sim.authorization.datatypes;

import jakarta.xml.bind.annotation.XmlEnumValue;
import java.lang.reflect.Field;
import java.util.*;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.authorization.interfaces.C2SimClaims;
import org.jose4j.jwt.JwtClaims;

public class ClaimValueListEnumSet<E extends Enum<E>> {

  private final Set<E> enumSet;
  private final Class<E> enumClass;

  public ClaimValueListEnumSet(Class<E> enumClass, Set<E> elementsInSet) {
    this.enumClass = enumClass;
    this.enumSet = elementsInSet;
  }

  public void add(E value) {
    enumSet.add(value);
  }

  public boolean getHasPermissionFor(E enumValue) {
    return enumSet.contains(enumValue);
  }

  public String getEnumTextValue(E enumValue) {
    try {
      Field field = enumClass.getField(enumValue.name());
      XmlEnumValue annotation = field.getAnnotation(XmlEnumValue.class);
      return annotation != null ? annotation.value() : enumValue.name();
    } catch (NoSuchFieldException e) {
      return enumValue.name();
    }
  }

  // Build string to enum value map
  private static <E extends Enum<E>> Map<String, E> buildXmlValueMap(Class<E> enumClass) {
    Map<String, E> result = new HashMap<>();
    for (E constant : enumClass.getEnumConstants()) {

      try {
        Field field = enumClass.getField(constant.name());
        XmlEnumValue annotation = field.getAnnotation(XmlEnumValue.class);
        String xmlValue = annotation != null ? annotation.value() : constant.name();
        result.put(xmlValue, constant);
      } catch (NoSuchFieldException e) {
        throw new IllegalArgumentException("Failed to reflect enum field: " + constant.name(), e);
      }
    }
    return result;
  }

  public String toText() {
    StringJoiner joiner = new StringJoiner(C2SimClaims.CLAIM_LIST_SEPARATOR);
    for (E constant : enumSet) {
      joiner.add(getEnumTextValue(constant));
    }
    return joiner.toString();
  }

  public static <E extends Enum<E>> Set<E> create(
      Class<E> enumClass, JwtClaims claims, String claimName) throws AuthorisationException {
    EnumSet<E> result = EnumSet.noneOf(enumClass);
    ClaimValueList claimValueList = ClaimValueList.create(claims, claimName);

    // Return empty set when claim is not allowed
    if (!claimValueList.getClaimIsValid()) {
      return EnumSet.noneOf(enumClass);
    }

    // Return all enumerations if any
    if (claimValueList.getClaimIsAny()) {
      return EnumSet.allOf(enumClass);
    }

    // Convert STRING list to ENUM SET
    Map<String, E> map = buildXmlValueMap(enumClass);
    for (String claimValue : claimValueList) {
      if (map.containsKey(claimValue)) {
        result.add(map.get(claimValue));
      } // else WARNING not valid enum value
    }
    return result;
  }
}
