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

package org.eclipse.jetty.util.security;

import java.io.Serializable;

/* ------------------------------------------------------------ */
/**
 * Credentials. The Credential class represents an abstract mechanism for
 * checking authentication credentials. A credential instance either represents
 * a secret, or some data that could only be derived from knowing the secret.
 * <p>
 * Often a Credential is related to a Password via a one way algorithm, so while
 * a Password itself is a Credential, a UnixCrypt or MD5 digest of a a password
 * is only a credential that can be checked against the password.
 * <p>
 * This class includes an implementation for unix Crypt an MD5 digest.
 * 
 * @see Password
 * 
 */
public abstract class Credential implements Serializable
{
    private static final long serialVersionUID = -7760551052768181572L;

    /* ------------------------------------------------------------ */
    /**
     * Check a credential
     * 
     * @param credentials The credential to check against. This may either be
     *                another Credential object, a Password object or a String
     *                which is interpreted by this credential.
     * @return True if the credentials indicated that the shared secret is known
     *         to both this Credential and the passed credential.
     */
    public abstract boolean check(Object credentials);
}
