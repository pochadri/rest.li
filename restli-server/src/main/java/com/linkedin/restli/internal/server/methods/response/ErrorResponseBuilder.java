/*
   Copyright (c) 2012 LinkedIn Corp.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

/**
 * $Id: $
 */

package com.linkedin.restli.internal.server.methods.response;

import com.linkedin.restli.server.ErrorResponseFormat;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import com.linkedin.r2.message.rest.RestRequest;
import com.linkedin.restli.common.ErrorResponse;
import com.linkedin.restli.common.RestConstants;
import com.linkedin.restli.internal.server.RoutingResult;
import com.linkedin.restli.server.RestLiServiceException;

/**
* @author Josh Walker
* @version $Revision: $
*/
public final class ErrorResponseBuilder implements RestLiResponseBuilder
{
  public static final String DEFAULT_INTERNAL_ERROR_MESSAGE = "Error in application code";
  private final ErrorResponseFormat _errorResponseFormat;
  private final String _internalErrorMessage;

  public ErrorResponseBuilder()
  {
    this(ErrorResponseFormat.defaultFormat());
  }

  public ErrorResponseBuilder(ErrorResponseFormat errorResponseFormat)
  {
    this(errorResponseFormat, DEFAULT_INTERNAL_ERROR_MESSAGE);
  }

  public ErrorResponseBuilder(ErrorResponseFormat errorResponseFormat, String internalErrorMessage)
  {
    _errorResponseFormat = errorResponseFormat;
    _internalErrorMessage = internalErrorMessage;
  }

  public String getInternalErrorMessage()
  {
    return _internalErrorMessage;
  }

  @Override
  public PartialRestResponse buildResponse(final RestRequest request,
                                           final RoutingResult routingResult,
                                           final Object object,
                                           final Map<String, String> headers)
  {
    RestLiServiceException result = (RestLiServiceException) object;
    ErrorResponse er = buildErrorResponse(result);

    if(_errorResponseFormat.showHeaders())
    {
      headers.put(RestConstants.HEADER_LINKEDIN_TYPE, er.getClass().getName());
      headers.put(RestConstants.HEADER_LINKEDIN_ERROR_RESPONSE,
                  RestConstants.HEADER_VALUE_ERROR_APPLICATION);
    }
    return new PartialRestResponse(result.getStatus(), er);
  }

  public ErrorResponse buildErrorResponse(RestLiServiceException result)
  {
    ErrorResponse er = new ErrorResponse();
    if(_errorResponseFormat.showStatusCodeInBody())
    {
      er.setStatus(result.getStatus().getCode());
    }

    if (_errorResponseFormat.showMessage() && result.getMessage() != null)
    {
      er.setMessage(result.getMessage());
    }
    if (_errorResponseFormat.showServiceErrorCode() && result.hasServiceErrorCode())
    {
      er.setServiceErrorCode(result.getServiceErrorCode());
    }
    if (_errorResponseFormat.showDetails() && result.hasErrorDetails())
    {
      er.setErrorDetails(result.getErrorDetails());
    }

    if(_errorResponseFormat.showStacktrace())
    {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      result.printStackTrace(pw);
      er.setStackTrace(sw.toString());

      er.setExceptionClass(result.getClass().getName());
    }

    return er;
  }
}
