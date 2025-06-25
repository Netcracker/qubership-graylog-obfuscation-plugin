# The sensitive data description
## 1. SSN (Social Security Number)
**Document**:
    - [https://www.usrecordsearch.com/ssn.htm](https://www.usrecordsearch.com/ssn.htm)
    - [https://en.wikipedia.org/wiki/Social_Security_number](https://en.wikipedia.org/wiki/Social_Security_number)  
Three number fields are separated by dash

**Format**: `AAA-GG-SSSS`
where:
* `AAA` - is area number. The value `000` is excluded by default.
    Values 650-699 is not assigned.
    Values 700-728 is Railroad workers through 1963, then discontinued.
    Values 729-799 is not assigned.
    Values 800-999 is not valid.
    Effective June 25, 2011, the SSA assigns SSNs randomly and allows for the assignment of area numbers between 734 and 749 and above 772 through the 800s.  
* `GG` - is group number. The value 00 is excluded by default.
* `SSSS` - is serial number. The value 0000 is excluded by default.
From left/right side the number, letter or dash with number or letter cannot be.
## 2. ICCID (Integrated Circuit Card Identifier)
**Document**:
* [https://www.theiphonewiki.com/wiki/ICCID](https://www.theiphonewiki.com/wiki/ICCID)

**Format**: `MMCC IINN NNNN NNNN NN C x`, Length 19-20 digits
where:
* `MM` - Constant, 89 for telecom operators.
* `CC` - Country code (i.e. 61 = Australia, 86 = China). Length 1-3
* `II` - Issuer identifier. Length 1-4
* `N`{11-12} - Account id (SIM number).
* `C` - Checksum calculated from the other 19 digits using the Luhn algorithm.
## 3. PN (Password Number)
**Document**:
* Depend on country

**Format**:  
US: 9 digits  
British: 9 digits  
Japan: 2 latin alpha, 7 digits  
