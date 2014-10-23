/*
 * Copyright 2014 Yubico.
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Use of this source code is governed by a BSD-style
 * license that can be found in the LICENSE file or at
 * https://developers.google.com/open-source/licenses/bsd
 */

package com.yubico.u2f;

import com.google.common.collect.ImmutableSet;
import com.yubico.u2f.data.DeviceRegistration;
import com.yubico.u2f.data.messages.AuthenticateResponse;
import com.yubico.u2f.data.messages.RegisterResponse;
import com.yubico.u2f.data.messages.StartedAuthentication;
import com.yubico.u2f.data.messages.StartedRegistration;
import com.yubico.u2f.exceptions.U2fException;
import com.yubico.u2f.testdata.AcmeKey;
import com.yubico.u2f.testdata.TestVectors;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static com.yubico.u2f.testdata.GnubbyKey.ATTESTATION_CERTIFICATE;
import static com.yubico.u2f.testdata.TestVectors.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

public class U2FTest {
  final HashSet<String> allowedOrigins = new HashSet<String>();

  @Before
  public void setup() throws Exception {
    initMocks(this);
    allowedOrigins.add("http://example.com");
  }

  @Test
  public void finishRegistration() throws Exception {
    StartedRegistration startedRegistration = new StartedRegistration(SERVER_CHALLENGE_REGISTER_BASE64, APP_ID_ENROLL);

    U2F.finishRegistration(startedRegistration, new RegisterResponse(TestVectors.REGISTRATION_RESPONSE_DATA_BASE64, CLIENT_DATA_REGISTER_BASE64), TRUSTED_DOMAINS);
  }

  @Test
  public void finishRegistration2() throws Exception {
    StartedRegistration startedRegistration = new StartedRegistration(SERVER_CHALLENGE_REGISTER_BASE64, APP_ID_ENROLL);

    DeviceRegistration deviceRegistration = U2F.finishRegistration(startedRegistration, new RegisterResponse(AcmeKey.REGISTRATION_DATA_BASE64, AcmeKey.CLIENT_DATA_BASE64), TRUSTED_DOMAINS);

    assertEquals(new DeviceRegistration(AcmeKey.KEY_HANDLE, AcmeKey.USER_PUBLIC_KEY, AcmeKey.ATTESTATION_CERTIFICATE, 0), deviceRegistration);
  }

  @Test
  public void finishAuthentication() throws Exception {
    StartedAuthentication startedAuthentication = new StartedAuthentication(SERVER_CHALLENGE_SIGN_BASE64, APP_ID_SIGN, KEY_HANDLE_BASE64);

    AuthenticateResponse tokenResponse = new AuthenticateResponse(CLIENT_DATA_AUTHENTICATE_BASE64,
        SIGN_RESPONSE_DATA_BASE64, SERVER_CHALLENGE_SIGN_BASE64);

    U2F.finishAuthentication(startedAuthentication, tokenResponse, new DeviceRegistration(KEY_HANDLE, USER_PUBLIC_KEY_SIGN_HEX, ATTESTATION_CERTIFICATE, 0), allowedOrigins);
  }


  @Test(expected = U2fException.class)
  public void finishAuthentication_badOrigin() throws Exception {
    Set<String> allowedOrigins = ImmutableSet.of("some-other-domain.com");
    StartedAuthentication authentication = new StartedAuthentication(SERVER_CHALLENGE_SIGN_BASE64,
            APP_ID_SIGN, KEY_HANDLE_BASE64);

    AuthenticateResponse response = new AuthenticateResponse(CLIENT_DATA_AUTHENTICATE_BASE64,
        SIGN_RESPONSE_DATA_BASE64, SERVER_CHALLENGE_SIGN_BASE64);

    U2F.finishAuthentication(authentication, response, new DeviceRegistration(KEY_HANDLE, USER_PUBLIC_KEY_SIGN_HEX, ATTESTATION_CERTIFICATE, 0), allowedOrigins);
  }
}
