/*
MariaDB Client for Java

Copyright (c) 2012-2014 Monty Program Ab.

This library is free software; you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the Free
Software Foundation; either version 2.1 of the License, or (at your option)
any later version.

This library is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
for more details.

You should have received a copy of the GNU Lesser General Public License along
with this library; if not, write to Monty Program Ab info@montyprogram.com.

This particular MariaDB Client for Java file is work
derived from a Drizzle-JDBC. Drizzle-JDBC file which is covered by subject to
the following copyright and notice provisions:

Copyright (c) 2009-2011, Marcus Eriksson

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:
Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of the driver nor the names of its contributors may not be
used to endorse or promote products derived from this software without specific
prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS  AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
OF SUCH DAMAGE.
*/

package org.mariadb.jdbc.internal;

import org.mariadb.jdbc.internal.queryresults.resultset.MariaSelectResultSet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Types;


public enum MariaDbType {
    OLDDECIMAL(0, Types.DECIMAL, BigDecimal.class.getName()),
    TINYINT(1, Types.SMALLINT, Integer.class.getName()),
    SMALLINT(2, Types.SMALLINT, Integer.class.getName()),
    INTEGER(3, Types.INTEGER, Integer.class.getName()),
    FLOAT(4, Types.REAL, Float.class.getName()),
    DOUBLE(5, Types.DOUBLE, Double.class.getName()),
    NULL(6, Types.NULL, String.class.getName()),
    TIMESTAMP(7, Types.TIMESTAMP, java.sql.Timestamp.class.getName()),
    BIGINT(8, Types.BIGINT, Long.class.getName()),
    MEDIUMINT(9, Types.INTEGER, Integer.class.getName()),
    DATE(10, Types.DATE, java.sql.Date.class.getName()),
    TIME(11, Types.TIME, java.sql.Time.class.getName()),
    DATETIME(12, Types.TIMESTAMP, java.sql.Timestamp.class.getName()),
    YEAR(13, Types.SMALLINT, Short.class.getName()),
    NEWDATE(14, Types.DATE, java.sql.Date.class.getName()),
    VARCHAR(15, Types.VARCHAR, String.class.getName()),
    BIT(16, Types.BIT, "[B"),
    DECIMAL(246, Types.DECIMAL, BigDecimal.class.getName()),
    ENUM(247, Types.VARCHAR, String.class.getName()),
    SET(248, Types.VARCHAR, String.class.getName()),
    TINYBLOB(249, Types.VARBINARY, "[B"),
    MEDIUMBLOB(250, Types.VARBINARY, "[B"),
    LONGBLOB(251, Types.LONGVARBINARY, "[B"),
    BLOB(252, Types.LONGVARBINARY, "[B"),
    VARSTRING(253, Types.VARCHAR, String.class.getName()),
    STRING(254, Types.VARCHAR, String.class.getName()),
    GEOMETRY(255, Types.VARBINARY, "[B");

    static MariaDbType[] typeMap;

    static {
        typeMap = new MariaDbType[256];
        for (MariaDbType v : values()) {
            typeMap[v.mysqlType] = v;
        }
    }

    private final int javaType;
    private final int mysqlType;
    private final String className;

    MariaDbType(int mysqlType, int javaType, String className) {
        this.javaType = javaType;
        this.mysqlType = mysqlType;
        this.className = className;
    }

    /**
     * Is type numeric.
     * @param type mariadb type
     * @return true if type is numeric
     */
    public static boolean isNumeric(MariaDbType type) {
        switch (type) {
            case OLDDECIMAL:
            case TINYINT:
            case SMALLINT:
            case INTEGER:
            case FLOAT:
            case DOUBLE:
            case BIGINT:
            case MEDIUMINT:
            case BIT:
            case DECIMAL:
                return true;
            default:
                return false;
        }
    }

    public String getClassName() {
        return className;
    }

    /**
     * Get class name.
     * @param type type
     * @param len len
     * @param signed signed
     * @param binary binary
     * @param flags flags
     * @return class name
     */
    public static String getClassName(MariaDbType type, int len, boolean signed, boolean binary, int flags) {
        switch (type) {
            case TINYINT:
                if (len == 1 && ((flags & MariaSelectResultSet.TINYINT1_IS_BIT) != 0)) {
                    return Boolean.class.getName();
                }
                return Integer.class.getName();
            case INTEGER:
                return (signed) ? Integer.class.getName() : Long.class.getName();
            case BIGINT:
                return (signed) ? Long.class.getName() : BigInteger.class.getName();
            case YEAR:
                if ((flags & MariaSelectResultSet.YEAR_IS_DATE_TYPE) != 0) {
                    return java.sql.Date.class.getName();
                }
                return Short.class.getName();
            case BIT:
                return (len == 1) ? Boolean.class.getName() : "[B";
            case STRING:
            case VARCHAR:
            case VARSTRING:
                return binary ? "[B" : String.class.getName();
            default:
                break;
        }
        return type.getClassName();
    }

    /**
     * Get columnTypeName.
     * @param type type
     * @param len len
     * @param signed signed
     * @param binary binary
     * @return type
     */
    public static String getColumnTypeName(MariaDbType type, long len, boolean signed, boolean binary) {
        switch (type) {
            case SMALLINT:
            case MEDIUMINT:
            case INTEGER:
            case BIGINT:
                if (!signed) {
                    return type.getTypeName() + " UNSIGNED";
                } else {
                    return type.getTypeName();
                }
            case BLOB:
                 /*
                   map to different blob types based on datatype length
                   see http://dev.mysql.com/doc/refman/5.0/en/storage-requirements.html
                  */
                if (len < 0) {
                    return "LONGBLOB";
                } else if (len <= 255) {
                    return "TINYBLOB";
                } else if (len <= 65535) {
                    return "BLOB";
                } else if (len <= 16777215) {
                    return "MEDIUMBLOB";
                } else {
                    return "LONGBLOB";
                }
            case VARSTRING:
            case VARCHAR:
                if (binary) {
                    return "VARBINARY";
                }
                return "VARCHAR";
            case STRING:
                if (binary) {
                    return "BINARY";
                }
                return "CHAR";
            default:
                return type.getTypeName();
        }
    }

    /**
     * Convert server Type to server type.
     * @param typeValue type value
     * @return MariaDb type
     */
    public static MariaDbType fromServer(int typeValue) {

        MariaDbType mariaDbType = typeMap[typeValue];

        if (mariaDbType == null) {
            /*
              Potential fallback for types that are not implemented.
              Should not be normally used.
             */
            mariaDbType = BLOB;
        }
        return mariaDbType;
    }

    /**
     * Convert javatype to MariaDbType.
     * @param javaType javatype value
     * @return mariaDb type value
     */
    public static MariaDbType toServer(int javaType) {
        for (MariaDbType v : values()) {
            if (v.javaType == javaType) {
                return v;
            }
        }
        return MariaDbType.BLOB;
    }

    public int getSqlType() {
        return javaType;
    }

    public String getTypeName() {
        return name();
    }

    public int getType() {
        return mysqlType;
    }

}