/*
 * Copyright (C) 2012 Joan Goyeau & Guillaume Demurger
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.umlv.qroxy;

import fr.umlv.qroxy.HttpHeader.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Scanner;
import org.junit.Test;
import org.junit.Assert;

/**
 *
 * @author joan
 */
public class HttpHeaderTest {

    @Test
    public void newHttpHeaderByScanner() throws MalformedHttpHeaderException, MalformedURLException, ParseException {
        Scanner scanner = new Scanner("GET http://noelie.dubail.free.fr/ HTTP/1.1\r\n"
                + "Host: noelie.dubail.free.fr\r\n"
                + "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:10.0.2) Gecko/20100101 Firefox/10.0.2\r\n"
                + "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\r\n"
                + "Accept-Language: fr,fr-fr;q=0.8,en-us;q=0.5,en;q=0.3\r\n"
                + "Accept-Encoding: gzip, deflate\r\n"
                + "Connection: keep-alive\r\n"
                + "Cookie: d9ba24b46d16ceadbfc182f1d2e8c5e5=a83a11c35b4e77ba13fbd0225413e2ef\r\n"
                + "If-Modified-Since: Tue, 24 Apr 2012 03:14:22 GMT\r\n"
                + "\r\n");
        HttpHeader httpHeader = new HttpHeader(scanner);
        
        HttpHeader httpHeaderExpected = new HttpHeader(Method.GET, new URL("http://noelie.dubail.free.fr/"), null, new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).parse("Tue, 24 Apr 2012 03:14:22 GMT"));
        Assert.assertEquals(httpHeaderExpected, httpHeader);
    }
}
