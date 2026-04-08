package org.c2sim.authorization.utils;

import java.util.EnumSet;
import java.util.Set;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.authorization.interfaces.TextEnum;

/** Helper class to handle enumerations */
public class EnumUtil {

  private EnumUtil() {
    throw new AssertionError("Only static functions");
  }

  /**
   * Convert string array to enum array
   *
   * @param enumClass The expected enumeration type
   * @param texts The enumerations as text
   * @return Set of enumerations (converted from text)
   * @param <E> The enumeration type
   */
  public static <E extends Enum<E> & TextEnum> Set<E> fromTextSet(
      Class<E> enumClass, Set<String> texts) {
    EnumSet<E> result = EnumSet.noneOf(enumClass);
    for (E constant : enumClass.getEnumConstants()) {
      if (texts.stream().anyMatch(t -> t.equalsIgnoreCase(constant.getText()))) {
        result.add(constant);
      }
    }
    return result;
  }

  /**
   * Convert text to enumeration
   *
   * @param enumClass The expected enumeration class
   * @param text The enumeration as text
   * @return The parsed enum
   * @param <E> The enumeration type
   * @throws AuthorisationException Failed to convert text to enum (misused AuthorisationException
   *     for this)
   */
  public static <E extends Enum<E> & TextEnum> E fromText(Class<E> enumClass, String text)
      throws AuthorisationException {
    for (E constant : enumClass.getEnumConstants()) {
      if (text.equalsIgnoreCase(constant.getText())) {
        return constant;
      }
    }
    throw new AuthorisationException(
        String.format("The value '%s' can not be mapped on enum", text));
  }
}
