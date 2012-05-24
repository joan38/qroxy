/*
 * Copyright (C) 2012 Joan Goyeau <joan.goyeau@gmail.com>
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

/**
 * 10 Status Code Definitions (RFC 2616).
 * 
 * Each Status-Code is described below, including a description of which
 * method(s) it can follow and any metainformation required in the
 * response.
 * 
 * @author Joan Goyeau <joan.goyeau@gmail.com>
 */
public enum HttpStatusCode {

    /**
     * Informational 1xx
     * 
     * This class of status code indicates a provisional response,
     * consisting only of the Status-Line and optional headers, and is
     * terminated by an empty line. There are no required headers for this
     * class of status code. Since HTTP/1.0 did not define any 1xx status
     * codes, servers MUST NOT send a 1xx response to an HTTP/1.0 client
     * except under experimental conditions.
     * 
     * A client MUST be prepared to accept one or more 1xx status responses
     * prior to a regular response, even if the client does not expect a 100
     * (Continue) status message. Unexpected 1xx status responses MAY be
     * ignored by a user agent.
     * 
     * Proxies MUST forward 1xx responses, unless the connection between the
     * proxy and its client has been closed, or unless the proxy itself
     * requested the generation of the 1xx response. (For example, if a
     * proxy adds a "Expect: 100-continue" field when it forwards a request,
     * then it need not forward the corresponding 100 (Continue)
     * response(s).)
     */
    /**
     * 100 Continue
     * 
     * The client SHOULD continue with its request. This interim response is
     * used to inform the client that the initial part of the request has
     * been received and has not yet been rejected by the server. The client
     * SHOULD continue by sending the remainder of the request or, if the
     * request has already been completed, ignore this response. The server
     * MUST send a final response after the request has been completed. See
     * section 8.2.3 (RFC 2616) for detailed discussion of the use and handling
     * of this status code.
     */
    STATUS_100,
    /**
     * 101 Switching Protocols
     * 
     * The server understands and is willing to comply with the client's
     * request, via the Upgrade message header field (section 14.42 RFC 2616),
     *  for a change in the application protocol being used on this connection.
     *  The server will switch protocols to those defined by the response's
     * Upgrade header field immediately after the empty line which
     * terminates the 101 response.
     * 
     * The protocol SHOULD be switched only when it is advantageous to do
     * so. For example, switching to a newer version of HTTP is advantageous
     * over older versions, and switching to a real-time, synchronous
     * protocol might be advantageous when delivering resources that use
     * such features.
     */
    STATUS_101,
    /**
     * Successful 2xx
     * 
     * The client SHOULD continue with its request. This interim response is
     * used to inform the client that the initial part of the request has
     * been received and has not yet been rejected by the server. The client
     * SHOULD continue by sending the remainder of the request or, if the
     * request has already been completed, ignore this response. The server
     * MUST send a final response after the request has been completed. See
     * section 8.2.3 (RFC 2616) for detailed discussion of the use and handling
     * of this status code.
     */
    /**
     * 200 OK
     * 
     * The request has succeeded. The information returned with the response
     * is dependent on the method used in the request, for example:
     * 
     * GET    an entity corresponding to the requested resource is sent in
     *        the response;
     * 
     * HEAD   the entity-header fields corresponding to the requested
     *        resource are sent in the response without any message-body;
     * 
     * POST   an entity describing or containing the result of the action;
     * 
     * TRACE  an entity containing the request message as received by the
     *        end server.
     */
    STATUS_200,
    /**
     * 201 Created
     * 
     * The request has been fulfilled and resulted in a new resource being
     * created. The newly created resource can be referenced by the URI(s)
     * returned in the entity of the response, with the most specific URI
     * for the resource given by a Location header field. The response
     * SHOULD include an entity containing a list of resource
     * characteristics and location(s) from which the user or user agent can
     * choose the one most appropriate. The entity format is specified by
     * the media type given in the Content-Type header field. The origin
     * server MUST create the resource before returning the 201 status code.
     * If the action cannot be carried out immediately, the server SHOULD
     * respond with 202 (Accepted) response instead.
     * 
     * A 201 response MAY contain an ETag response header field indicating
     * the current value of the entity tag for the requested variant just
     * created, see section 14.19 (RFC 2616).
     */
    STATUS_201,
    /**
     * 202 Accepted
     * 
     * The request has been accepted for processing, but the processing has
     * not been completed.  The request might or might not eventually be
     * acted upon, as it might be disallowed when processing actually takes
     * place. There is no facility for re-sending a status code from an
     * asynchronous operation such as this.
     * 
     * The 202 response is intentionally non-committal. Its purpose is to
     * allow a server to accept a request for some other process (perhaps a
     * batch-oriented process that is only run once per day) without
     * requiring that the user agent's connection to the server persist
     * until the process is completed. The entity returned with this
     * response SHOULD include an indication of the request's current status
     * and either a pointer to a status monitor or some estimate of when the
     * user can expect the request to be fulfilled.
     */
    STATUS_202,
    /**
     * 203 Non-Authoritative Information
     * 
     * The returned metainformation in the entity-header is not the
     * definitive set as available from the origin server, but is gathered
     * from a local or a third-party copy. The set presented MAY be a subset
     * or superset of the original version. For example, including local
     * annotation information about the resource might result in a superset
     * of the metainformation known by the origin server. Use of this
     * response code is not required and is only appropriate when the
     * response would otherwise be 200 (OK).
     */
    STATUS_203,
    /**
     * 204 No Content
     * 
     * The server has fulfilled the request but does not need to return an
     * entity-body, and might want to return updated metainformation. The
     * response MAY include new or updated metainformation in the form of
     * entity-headers, which if present SHOULD be associated with the
     * requested variant.
     * 
     * If the client is a user agent, it SHOULD NOT change its document view
     * from that which caused the request to be sent. This response is
     * primarily intended to allow input for actions to take place without
     * causing a change to the user agent's active document view, although
     * any new or updated metainformation SHOULD be applied to the document
     * currently in the user agent's active view.
     * 
     * The 204 response MUST NOT include a message-body, and thus is always
     * terminated by the first empty line after the header fields.
     */
    STATUS_204,
    /**
     * 205 Reset Content
     * 
     * The server has fulfilled the request and the user agent SHOULD reset
     * the document view which caused the request to be sent. This response
     * is primarily intended to allow input for actions to take place via
     * user input, followed by a clearing of the form in which the input is
     * given so that the user can easily initiate another input action. The
     * response MUST NOT include an entity.
     */
    STATUS_205,
    /**
     * 206 Partial Content
     * 
     * The server has fulfilled the partial GET request for the resource.
     * The request MUST have included a Range header field (section 14.35 RFC
     * 2616) indicating the desired range, and MAY have included an If-Range
     * header field (section 14.27 RFC 2616) to make the request conditional.
     * 
     * The response MUST include the following header fields:
     * 
     *    - Either a Content-Range header field (section 14.16 RFC 2616)
     *      indicating the range included with this response, or a
     *      multipart/byteranges Content-Type including Content-Range fields
     *      for each part. If a Content-Length header field is present in the
     *      response, its value MUST match the actual number of OCTETs
     *      transmitted in the message-body.
     * 
     *    - Date
     * 
     *    - ETag and/or Content-Location, if the header would have been sent
     *      in a 200 response to the same request
     * 
     *    - Expires, Cache-Control, and/or Vary, if the field-value might
     *      differ from that sent in any previous response for the same
     *       variant
     * 
     * If the 206 response is the result of an If-Range request that used a
     * strong cache validator (see section 13.3.3 RFC 2616), the response
     * SHOULD NOT include other entity-headers. If the response is the result
     * of an If-Range request that used a weak validator, the response MUST NOT
     * include other entity-headers; this prevents inconsistencies between
     * cached entity-bodies and updated headers. Otherwise, the response
     * MUST include all of the entity-headers that would have been returned
     * with a 200 (OK) response to the same request.
     * 
     * A cache MUST NOT combine a 206 response with other previously cached
     * content if the ETag or Last-Modified headers do not match exactly,
     * see 13.5.4.
     * 
     * A cache that does not support the Range and Content-Range headers
     * MUST NOT cache 206 (Partial) responses.
     */
    STATUS_206,

    /**
     * Redirection 3xx
     * 
     * This class of status code indicates that further action needs to be
     * taken by the user agent in order to fulfill the request.  The action
     * required MAY be carried out by the user agent without interaction
     * with the user if and only if the method used in the second request is
     * GET or HEAD. A client SHOULD detect infinite redirection loops, since
     * such loops generate network traffic for each redirection.
     * 
     * Note: previous versions of this specification recommended a
     * maximum of five redirections. Content developers should be aware
     * that there might be clients that implement such a fixed
     * limitation.
     */
    /**
     * 300 Multiple Choices
     */
    STATUS_300,
    /**
     * 301 Moved Permanently
     */
    STATUS_301,
    /**
     * 302 Found
     */
    STATUS_302,
    /**
     * 303 See Other
     */
    STATUS_303,
    /**
     * 304 Not Modified
     */
    STATUS_304,
    /**
     * 305 Use Proxy
     */
    STATUS_305,
    /**
     * 306 (Unused)
     */
    STATUS_306,
    /**
     * 307 Temporary Redirect
     */
    STATUS_307,
    /**
     * Client Error 4xx
     * 
     * The 4xx class of status code is intended for cases in which the
     * client seems to have erred. Except when responding to a HEAD request,
     * the server SHOULD include an entity containing an explanation of the
     * error situation, and whether it is a temporary or permanent
     * condition. These status codes are applicable to any request method.
     * User agents SHOULD display any included entity to the user.
     * 
     * If the client is sending data, a server implementation using TCP
     * SHOULD be careful to ensure that the client acknowledges receipt of
     * the packet(s) containing the response, before the server closes the
     * input connection. If the client continues sending data to the server
     * after the close, the server's TCP stack will send a reset packet to
     * the client, which may erase the client's unacknowledged input buffers
     * before they can be read and interpreted by the HTTP application.
     */
    /**
     * 400 Bad Request
     * 
     * The request could not be understood by the server due to malformed
     * syntax. The client SHOULD NOT repeat the request without
     * modifications.
     */
    STATUS_400,
    /**
     * 401 Unauthorized
     * 
     * The request requires user authentication. The response MUST include a
     * WWW-Authenticate header field (section 14.47) containing a challenge
     * applicable to the requested resource. The client MAY repeat the
     * request with a suitable Authorization header field (section 14.8 RFC 2616).
     * If the request already included Authorization credentials, then the 401
     * response indicates that authorization has been refused for those
     * credentials. If the 401 response contains the same challenge as the
     * prior response, and the user agent has already attempted
     * authentication at least once, then the user SHOULD be presented the
     * entity that was given in the response, since that entity might
     * include relevant diagnostic information. HTTP access authentication
     * is explained in "HTTP Authentication: Basic and Digest Access
     * Authentication" [43].
     */
    STATUS_401,
    /**
     * 402 Payment Required
     * 
     * This code is reserved for future use.
     */
    STATUS_402;
}
