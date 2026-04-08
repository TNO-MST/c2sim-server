package org.c2sim.authorization.datatypes;

import java.util.*;
import java.util.stream.Collectors;
import org.c2sim.authorization.exceptions.AuthorisationException;
import org.c2sim.authorization.interfaces.C2SimClaims;
import org.jose4j.jwt.JwtClaims;

/**
 * Models the C2SIM PEP "set of STRING" claim datatype extracted from a JWT Bearer token.
 *
 * <p>A {@code ClaimValueList} is a {@link HashSet} of trimmed string values with additional
 * semantics for the special cases defined by the C2SIM PEP standard:
 *
 * <ul>
 *   <li>{@link #ANY} — the literal value {@code "ANY"} in the JWT claim; permits any value.
 *   <li>{@link #CLAIM_NOT_IN_JWT_TOKEN} — the claim name is absent from the token.
 *   <li>{@link #CLAIM_IS_EMPTY_STRING} — the claim is present but is an empty string.
 *   <li>{@link #NOTHING_ALLOWED} — an explicit empty JSON array ({@code []}); nothing is allowed.
 * </ul>
 *
 * <p>The last three special cases are all treated as <em>unauthorized</em> during the authorization
 * check, but they are modelled as distinct instances so that callers can distinguish the reason for
 * rejection.
 *
 * <p>JWT claim values are parsed from two wire formats:
 *
 * <ul>
 *   <li>A semicolon-delimited text string — e.g. {@code "A;B;C"}
 *   <li>A JSON string array — e.g. {@code ["A","B","C"]}
 * </ul>
 *
 * <p>Instances are obtained via {@link #create(JwtClaims, String)} (from a live JWT) or {@link
 * #createWithTextNotation(String, String)} (from a raw string value).
 */
public class ClaimValueList extends HashSet<String> {

  /*

  Parses the datatype in C2SIM PEP standard 'set of STRING value'

  Notations:
  - Text string with ';' separator (example "A;B;C" )
  - JSON string with an STRING array (example ["A","B","C"])
  - The literal text 'ANY' will allow all values
  - An empty string is handled as UNAUTHORIZED (most libraries will remove empty claims)
  - An empty JSON array is handled as UNAUTHORIZED in authorization step (example [])
  - A missing claim is handled as UNAUTHORIZED in authorization step

  There is a difference between empty string and '[]'. The '[]' explicit defines that noting is allowed.

  This method converts from org.jos4j.jwt claim format to internal claim format (for set of STRING)
  Possible options for claimName in jwtClaims
  - Not found (not existing)
  - Found, datatype is text string
  - Found, datatype is json array string


   */

  // Special cases for 'set of STRING' datatype
  /**
   * Sentinel instance representing an explicit empty JSON array ({@code []}); nothing is permitted.
   * Treated as unauthorized during authorization.
   */
  private static class NothingAllowedClaimValueList extends ClaimValueList {
    private NothingAllowedClaimValueList() {}

    @Override
    public boolean add(String s) {
      throw new UnsupportedOperationException("Immutable");
    }
  }

  @SuppressWarnings("java:S2386") // Make this member "protected".
  public static final ClaimValueList NOTHING_ALLOWED =
      new NothingAllowedClaimValueList(); /* Nothing allowed */

  /**
   * Sentinel instance representing a claim that is absent from the JWT token. Treated as
   * unauthorized during authorization.
   */
  private static class ClaimNotInJwtTokenClaimValueList extends ClaimValueList {
    private ClaimNotInJwtTokenClaimValueList() {}

    @Override
    public boolean add(String s) {
      throw new UnsupportedOperationException("Immutable");
    }
  }

  @SuppressWarnings("java:S2386") // Make this member "protected".
  public static final ClaimValueList CLAIM_NOT_IN_JWT_TOKEN =
      new ClaimNotInJwtTokenClaimValueList();

  /**
   * Sentinel instance representing a claim whose value is an empty string. Treated as unauthorized
   * during authorization.
   */
  private static class StringIsEmptyTokenClaimValueList extends ClaimValueList {
    private StringIsEmptyTokenClaimValueList() {}

    @Override
    public boolean add(String s) {
      throw new UnsupportedOperationException("Immutable");
    }
  }

  @SuppressWarnings("java:S2386") // Make this member "protected".
  public static final ClaimValueList CLAIM_IS_EMPTY_STRING = new StringIsEmptyTokenClaimValueList();

  /**
   * Sentinel instance representing the wildcard value {@code "ANY"}. Grants permission for any
   * required value when checked via {@link #getHasPermissionFor(String)}.
   */
  private static class AnyClaimValueList extends ClaimValueList {
    private AnyClaimValueList() {}

    @Override
    public boolean add(String s) {
      throw new UnsupportedOperationException("Immutable");
    }
  }

  @SuppressWarnings("java:S2386") // Make this member "protected".
  public static final ClaimValueList ANY = new AnyClaimValueList();

  /** Creates an empty claim value list. */
  public ClaimValueList() {
    super();
  }

  /**
   * Creates an empty claim value list with the given initial capacity.
   *
   * @param initialCapacity the initial capacity of the underlying hash set
   */
  public ClaimValueList(int initialCapacity) {
    super(initialCapacity);
  }

  /**
   * Creates a claim value list pre-populated from an existing collection.
   *
   * @param c the collection of string values to add
   */
  public ClaimValueList(java.util.Collection<String> c) {
    super(c);
  }

  // Datatype is List
  private static ClaimValueList processDataTypeAsStringList(String claimName, List<?> claimValues)
      throws AuthorisationException {
    ClaimValueList values = new ClaimValueList();

    for (Object object : claimValues) {
      if (object instanceof String text) {
        values.add(text.trim());
      } else {
        throw new AuthorisationException(
            String.format(
                "Claim '%s': only strings allowed in array, found other datatype", claimName));
      }
    }
    if (values.contains(C2SimClaims.CLAIM_ANY)) {
      throw new AuthorisationException(
          String.format("Claim '%s': ANY is a reserved word", claimName));
    }
    return values;
  }

  // Parse string with ; as claim value
  private static ClaimValueList processDataTypeString(String claimName, String claimValue)
      throws AuthorisationException {
    // Datatype is text String; parse to array with separator
    // Beginning and ending spaces are trimmed
    claimValue = claimValue.trim();

    // Handle EMPTY
    if (claimValue.isEmpty()) {
      // Empty value claims will be removed by keycloak (so this should not happen)
      // Handle for safety
      return ClaimValueList.CLAIM_IS_EMPTY_STRING;
    }

    // Handle ANY
    if (C2SimClaims.CLAIM_ANY.equals(claimValue)) {
      // Special case ANY
      return ClaimValueList.ANY;
    }

    // Convert it from string to string array (with separator ; )
    var claimList =
        new ClaimValueList(
            Arrays.stream(claimValue.split(C2SimClaims.CLAIM_LIST_SEPARATOR))
                .map(String::trim)
                .collect(Collectors.toSet()));

    // Check array for ANY
    if (claimList.contains(C2SimClaims.CLAIM_ANY)) {
      throw new AuthorisationException(
          String.format(
              "Claim '%s' string list contains reserved word 'ANY' (only allowed as single value)",
              claimName));
    }
    return claimList;
  }

  /**
   * Parses a named claim from a {@link JwtClaims} object into a {@link ClaimValueList}.
   *
   * <p>The JWT claim value is handled as follows:
   *
   * <ul>
   *   <li>Absent — returns {@link #CLAIM_NOT_IN_JWT_TOKEN}
   *   <li>Text string — parsed as semicolon-delimited values; {@code "ANY"} returns {@link #ANY}
   *   <li>JSON string array — each element is trimmed and added; {@code "ANY"} in an array is
   *       rejected as a reserved word
   *   <li>Any other datatype — throws {@link AuthorisationException}
   * </ul>
   *
   * @param jwtClaims the parsed JWT claims object
   * @param claimName the name of the claim to extract
   * @return the parsed {@link ClaimValueList}, or one of the sentinel instances
   * @throws AuthorisationException if the claim value has an unsupported datatype or contains
   *     {@code "ANY"} in an invalid position
   */
  // Using static create allows to return predefined instances like ANY
  public static ClaimValueList create(JwtClaims jwtClaims, String claimName)
      throws AuthorisationException {

    // getClaimValue method easiest for unit testing
    var claimValue = jwtClaims.getClaimValue(claimName);
    switch (claimValue) {
      case null -> {
        // The 'claimName' is not found in JWT token
        // This is allowed; only when the claim in needed for authorization it will be handled as
        // UNAUTHORIZED
        return ClaimValueList.CLAIM_NOT_IN_JWT_TOKEN;
      }
      case String claimValueAsString -> {
        return processDataTypeString(claimName, claimValueAsString);
      }
      case List<?> claimValueAsStringArray -> {
        return processDataTypeAsStringList(claimName, claimValueAsStringArray);
      }
      default ->
          throw new AuthorisationException(
              String.format(
                  "Claim '%s' has invalid datatype "
                      + " (allowed datatype: string with '%s' as separator OR json array of strings ",
                  claimName, C2SimClaims.CLAIM_LIST_SEPARATOR));
    }
  }

  /**
   * Parses a claim value supplied as a raw semicolon-delimited text string.
   *
   * <p>Useful for constructing {@link ClaimValueList} instances in tests or from non-JWT sources
   * without a full {@link JwtClaims} object.
   *
   * @param claimName the logical claim name (used only in exception messages)
   * @param claimValue the raw semicolon-delimited string value
   * @return the parsed {@link ClaimValueList}, or {@link #ANY} / {@link #CLAIM_IS_EMPTY_STRING} for
   *     the corresponding special values
   * @throws AuthorisationException if the value contains {@code "ANY"} in an invalid position
   */
  //
  public static ClaimValueList createWithTextNotation(String claimName, String claimValue)
      throws AuthorisationException {
    return processDataTypeString(claimName, claimValue);
  }

  /**
   * Serialises this list back to its text representation.
   *
   * <p>Returns {@code "[]"} for {@link #NOTHING_ALLOWED}, {@code "ANY"} for {@link #ANY}, or the
   * values joined by {@link C2SimClaims#CLAIM_LIST_SEPARATOR} otherwise.
   *
   * @return the text representation of this claim value list
   */
  public String toText() {
    if (getIsNothingAllowed()) {
      return "[]";
    } else if (getClaimIsAny()) {
      return C2SimClaims.CLAIM_ANY;
    }
    return String.join(C2SimClaims.CLAIM_LIST_SEPARATOR, this);
  }

  /**
   * Returns {@code true} if this list grants permission for any value in the given set.
   *
   * @param requiredClaimValue the set of required values to check
   * @return {@code true} always (not yet fully implemented)
   */
  public boolean getHasPermissionFor(Set<String> requiredClaimValue) {
    Objects.requireNonNull(requiredClaimValue);
    return true;
  }

  /**
   * Returns {@code true} if this list grants permission for the given required value.
   *
   * <p>Returns {@code true} when the claim {@link #getClaimIsAny() is ANY} or when this set
   * contains {@code requiredClaimValue}. Returns {@code false} when the claim is not valid (absent,
   * empty, or nothing-allowed).
   *
   * @param requiredClaimValue the value that must be permitted
   * @return {@code true} if the required value is allowed by this claim
   */
  public boolean getHasPermissionFor(String requiredClaimValue) {
    Objects.requireNonNull(requiredClaimValue);
    if (getClaimIsValid()) {
      if (getClaimIsAny()) {
        return true;
      } else {
        return this.contains(requiredClaimValue);
      }
    } else {
      return false;
    }
  }

  /**
   * Returns {@code true} if this instance represents a claim that was present in the JWT token.
   *
   * @return {@code false} only when this instance is {@link #CLAIM_NOT_IN_JWT_TOKEN}
   */
  public boolean getClaimIsPresentInJwtToken() {
    // Are we in special instance ClaimNotInJWT?
    return this != CLAIM_NOT_IN_JWT_TOKEN;
  }

  /**
   * Returns {@code true} if this instance represents a claim whose JWT value was an empty string.
   *
   * @return {@code true} only when this instance is {@link #CLAIM_IS_EMPTY_STRING}
   */
  public boolean getClaimIsEmptyString() {
    // Are we in special instance ClaimNotInJWT?
    return this == CLAIM_IS_EMPTY_STRING;
  }

  /**
   * Returns {@code true} if this instance represents the wildcard {@code "ANY"} value.
   *
   * @return {@code true} only when this instance is {@link #ANY}
   */
  public boolean getClaimIsAny() {
    return this == ANY;
  }

  /**
   * Returns {@code true} if this instance represents an explicitly empty permission set (i.e.
   * nothing is allowed).
   *
   * @return {@code true} only when this instance is {@link #NOTHING_ALLOWED}
   */
  public boolean getIsNothingAllowed() {
    // Are we in special instance ClaimNotInJWT?
    return this == NOTHING_ALLOWED;
  }

  /**
   * Returns {@code true} if this claim is valid for an authorization check.
   *
   * <p>A claim is valid when it is present in the JWT, is not an empty string, and is not the
   * nothing-allowed sentinel. Only valid claims should be evaluated in permission checks.
   *
   * @return {@code true} if the claim can be used for authorization decisions
   */
  public boolean getClaimIsValid() {

    return ((getClaimIsPresentInJwtToken())
        && (!getClaimIsEmptyString())
        && (!getIsNothingAllowed()));
  }
}
