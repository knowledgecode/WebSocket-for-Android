//
//  ========================================================================
//  Copyright (c) 1995-2015 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.util;

import java.io.IOException;

/* ------------------------------------------------------------ */
/**
 * TYPE Utilities.
 * Provides various static utiltiy methods for manipulating types and their
 * string representations.
 *
 * @since Jetty 4.1
 */
public class TypeUtil
{
    /* ------------------------------------------------------------ */
    /**
     * @param c An ASCII encoded character 0-9 a-f A-F
     * @return The byte value of the character 0-16.
     */
    public static byte convertHexDigit( byte c )
    {
        byte b = (byte)((c & 0x1f) + ((c >> 6) * 0x19) - 0x10);
        if (b<0 || b>15)
            throw new IllegalArgumentException("!hex "+c);
        return b;
    }

    /* ------------------------------------------------------------ */
    public static void toHex(byte b,Appendable buf)
    {
        try
        {
            int d=0xf&((0xF0&b)>>4);
            buf.append((char)((d>9?('A'-10):'0')+d));
            d=0xf&b;
            buf.append((char)((d>9?('A'-10):'0')+d));
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /* ------------------------------------------------------------ */
    public static void toHex(int value,Appendable buf) throws IOException
    {
        int d=0xf&((0xF0000000&value)>>28);
        buf.append((char)((d>9?('A'-10):'0')+d));
        d=0xf&((0x0F000000&value)>>24);
        buf.append((char)((d>9?('A'-10):'0')+d));
        d=0xf&((0x00F00000&value)>>20);
        buf.append((char)((d>9?('A'-10):'0')+d));
        d=0xf&((0x000F0000&value)>>16);
        buf.append((char)((d>9?('A'-10):'0')+d));
        d=0xf&((0x0000F000&value)>>12);
        buf.append((char)((d>9?('A'-10):'0')+d));
        d=0xf&((0x00000F00&value)>>8);
        buf.append((char)((d>9?('A'-10):'0')+d));
        d=0xf&((0x000000F0&value)>>4);
        buf.append((char)((d>9?('A'-10):'0')+d));
        d=0xf&value;
        buf.append((char)((d>9?('A'-10):'0')+d));

        Integer.toString(0,36);
    }
}
