package model;

import main.DataAccess;
import script.model.ScriptConstants;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class GearDataObject {
    private int[] bytes;

    boolean isBukiGet;
    int alwaysZero3;
    int alwaysZero4;
    int alwaysZeroOrOne;
    int unknownC;
    int unknownD;
    int variousFlags;
    int character;
    int alwaysZero1;
    int armorByte;
    int alwaysZero2;
    int formula;
    int power;
    int crit;
    int slots;
    int ability1;
    int ability2;
    int ability3;
    int ability4;

    boolean armor;
    boolean flag1;
    boolean flag2;
    boolean celestial;
    boolean brotherhood;

    public GearDataObject() {}

    public GearDataObject(int[] bytes) throws IOException {
        isBukiGet = (bytes.length == 16);
        this.bytes = bytes;
        if (isBukiGet) {
            mapBytesBukiGet();
        } else {
            mapBytesNormal();
        }
        mapFlags();
    }

    private void mapBytesNormal() {
        alwaysZero3 = bytes[0x00];
        alwaysZero4 = bytes[0x01];
        alwaysZeroOrOne = bytes[0x02];
        variousFlags = bytes[0x03];
        character = bytes[0x04];
        armorByte = bytes[0x05];
        alwaysZero1 = bytes[0x06];
        alwaysZero2 = bytes[0x07];
        formula = bytes[0x08];
        power = bytes[0x09];
        crit = bytes[0x0A];
        slots = bytes[0x0B];
        unknownC = bytes[0x0C];
        unknownD = bytes[0x0D];
        ability1 = read2Bytes(0x0E);
        ability2 = read2Bytes(0x10);
        ability3 = read2Bytes(0x12);
        ability4 = read2Bytes(0x14);
    }

    private void mapBytesBukiGet() {
        alwaysZeroOrOne = 1;
        variousFlags = bytes[0x00];
        character = bytes[0x01];
        armorByte = bytes[0x02];
        alwaysZero1 = bytes[0x03];
        formula = bytes[0x04];
        power = bytes[0x05];
        crit = bytes[0x06];
        slots = bytes[0x07];
        ability1 = read2Bytes(0x08);
        ability2 = read2Bytes(0x0A);
        ability3 = read2Bytes(0x0C);
        ability4 = read2Bytes(0x0E);
    }

    private void mapFlags() {
        armor = armorByte != 0;
        flag1 = (variousFlags & 0x01) > 0;
        flag2 = (variousFlags & 0x02) > 0;
        celestial = (variousFlags & 0x04) > 0;
        brotherhood = (variousFlags & 0x08) > 0;
    }

    @Override
    public String toString() {
        String specialType = getSpecialType();
        String abilityString = getAbilityString();
        return "{ " + ScriptConstants.getEnumMap("actor").get(character).name +
                ", " + (armor ? "Armor" : "Weapon") + (armorByte > 1 ? "[" + armorByte + "]" : "") +
                ", F=" + formula +
                ", Power=" + power +
                ", Crit=" + crit + '%' +
                ", Slots=" + slots + ' ' +
                abilityString +
                specialType +
                (flag2 ? ", Hidden in Menu" : "") +
                (brotherhood ? ", Brotherhood" : "") +
                (alwaysZero1 != 0 ? ", 1 not Zero!=" + formatUnknownByte(alwaysZero1) : "") +
                (alwaysZero2 != 0 ? ", 2 not Zero!=" + formatUnknownByte(alwaysZero2) : "") +
                (alwaysZero3 != 0 ? ", 3 not Zero!=" + formatUnknownByte(alwaysZero3) : "") +
                (alwaysZero4 != 0 ? ", 4 not Zero!=" + formatUnknownByte(alwaysZero4) : "") +
                (alwaysZeroOrOne > 1 ? ", byte 2 greater than 1!=" + formatUnknownByte(alwaysZeroOrOne) : "") +
                (unknownC != 0 ? ", UC=" + formatUnknownByte(unknownC) : "") +
                (unknownD != 0 ? ", UD=" + formatUnknownByte(unknownD) : "") +
                " }";
    }

    private String getAbilityString() {
        String abilityString = "[";
        String ability1Str = getGearAbilityName(ability1);
        String ability2Str = getGearAbilityName(ability2);
        String ability3Str = getGearAbilityName(ability3);
        String ability4Str = getGearAbilityName(ability4);
        if (slots >= 1) {
            abilityString += ability1Str;
            if (slots >= 2 || !ability2Str.equals("Empty")) {
                abilityString += ", " + (slots < 2 ? "!" : "") + ability2Str;
                if (slots >= 3 || !ability3Str.equals("Empty")) {
                    abilityString += ", " + (slots < 3 ? "!" : "") + ability3Str;
                    if (slots >= 4 || !ability4Str.equals("Empty")) {
                        abilityString += ", " + (slots < 4 ? "!" : "") + ability4Str;
                    }
                }
            }
        }
        abilityString += "]";
        return abilityString;
    }

    private String getSpecialType() {
        String specialType = "";
        if (flag1) {
            specialType = ", Special: ";
            if (brotherhood) {
                if (flag2) {
                    specialType += "Original Brotherhood";
                } else {
                    specialType += "Other Brotherhood?";
                }
                if (celestial) {
                    specialType += ", CL";
                }
            } else if (flag2) {
                specialType += "Aeon " + (celestial ? "Weapon" : "Armor");
            } else {
                specialType += "(No BH, No F2)";
            }
        } else if (celestial) {
            specialType = ", Celestial";
        }
        return specialType;
    }

    private static String formatUnknownByte(int bt) {
        return String.format("%02x", bt) + '=' + String.format("%03d", bt) + '(' + String.format("%8s", Integer.toBinaryString(bt)).replace(' ', '0') + ')';
    }

    private static String getGearAbilityName(int ability) {
        if (ability == 0x00FF) {
            return "Empty";
        } else {
            GearAbilityDataObject obj = DataAccess.getGearAbility(ability);
            if (obj == null) {
                return "null";
            } else {
                return obj.getName();
            }
        }
    }

    private int read2Bytes(int offset) {
        int val = bytes[offset];
        val += bytes[offset+1] * 0x100;
        return val;
    }
}
