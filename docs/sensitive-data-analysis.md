# Sensitive Data Descriptions

## SSN (Social Security Number)

**Documents**:
- [https://www.investopedia.com/terms/s/ssn.asp](https://www.investopedia.com/terms/s/ssn.asp)
- [https://en.wikipedia.org/wiki/Social_Security_number](https://en.wikipedia.org/wiki/Social_Security_number)

Three number fields are separated by dashes.

**Format**: `AAA-GG-SSSS`

where:
- `AAA` - Area number. Excludes `000`. Values 650-699 unassigned.
Values 700-728 for railroad workers through 1963 (discontinued).
Values 729-799 unassigned.
Values 800-999 invalid.
Since June 25, 2011, SSA assigns randomly, including 734-749 and above 772 through 800s.
- `GG` - Group number. Excludes `00`.
- `SSSS` - Serial number. Excludes `0000`.

Numbers cannot start or end with a letter, dash-number, or dash-letter from left/right sides.

## ICCID (Integrated Circuit Card Identifier)

**Document**:
- [https://www.theiphonewiki.com/wiki/ICCID](https://www.theiphonewiki.com/wiki/ICCID)

**Format**: `MMCC IINN NNNN NNNN NN C x`, 19-20 digits

where:
- `MM` - Constant `89` for telecom operators.
- `CC` - Country code (e.g., 61=Australia, 86=China), 1-3 digits.
- `II` - Issuer identifier, 1-4 digits.
- `N`{11-12} - Account ID (SIM number).
- `C` - Checksum via Luhn algorithm on prior 19 digits.

## PN (Password Number)

**Document**:
- Country-dependent.

**Formats**:
- US: 9 digits.
- UK: 9 digits.  
- Japan: 2 Latin letters + 7 digits.
