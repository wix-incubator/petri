package com.wixpress.common.petri

import java.util
import java.util.UUID
import javax.annotation.Resource
import javax.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}

import com.fasterxml.jackson.annotation.JsonProperty
import com.wixpress.common.petri.PageWithButtonController.labUserIdField
import com.wixpress.petri.amplitude.{AmplitudeAdapter, BaseAmplitudeEvent}
import com.wixpress.petri.laboratory.converters.StringConverter
import com.wixpress.petri.laboratory.http.LaboratoryFilter.PETRI_USER_INFO_STORAGE
import com.wixpress.petri.laboratory.{Laboratory, RegisteredUserInfoType, UserInfo, UserInfoStorage}
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.{RequestMapping, RequestMethod, ResponseBody}

@Controller
class PageWithButtonController {
  @Resource
  var amplitudeAdapter: AmplitudeAdapter = _

  @Resource
  var laboratory: Laboratory = _

  @RequestMapping(value = Array("/test"), method = Array(RequestMethod.GET), produces = Array("text/html"))
  @ResponseBody
  def test(request: HttpServletRequest, response: HttpServletResponse): String = {
    val theUserId = getOrPutUserId(request, response)
    val userInfo = request.getSession.getAttribute(PETRI_USER_INFO_STORAGE).asInstanceOf[UserInfoStorage]
    userInfo.write(copyUserWithNewId(UUID.fromString(theUserId), userInfo.read()))

    renderedPageForRegisteredUser(theUserId)
  }

  def copyUserWithNewId(userId: UUID, u:UserInfo) = new UserInfo(u.experimentsLog, userId, u.clientId, u.ip, u.url, u.userAgent, new RegisteredUserInfoType(userId), u.language, u.country, u.dateCreated, u.companyEmployee, u.anonymousExperimentsLog, u.isRecurringUser, u.experimentOverrides, u.isRobot, u.host, new util.HashMap[UUID, String](), u.potentialOtherUserExperimentsLogFromCookies, u.registeredUserExists, u.globalSessionId)

  private def renderedPageForRegisteredUser(userId:String) = {
    val color = colorFromExperiment()
    s"""
       |<html>
       |<head>
       |<script src="https://code.jquery.com/jquery-1.9.1.js"></script>
       |</head>
       |<body>
       |<div>
       |Welcome user $userId!
       |</div>
       |<div>
       |  <input type="button" name="buttonId" id="buttonId" value="${if (color == "red") "Don't " else ""}click here!" style="color: $color"/>
       |</div>
       |<div id="resultText">
       |</div>
       |<div id="userDetails">
       |</div>
       |</body>
       |<script>
       |$$(document).ready(function() {
       |    $$("#buttonId").click(function() {
       |       $$("#buttonId").hide();
       |       $$("#resultText").html("working on it... wait!");
       |       $$.post( "/buttonClicked", function(res) {
       |         $$("#resultText").html('Finished! checkout results <a href="https://amplitude.com/app/151746/funnels?fid=20206&sset=%7B%22byProp%22:%22a%22,%22segmentIndex%22:0%7D&sset=%7B%22byProp%22:%22b%22,%22segmentIndex%22:0%7D&cg=User&range=Last%207%20Days&i=1&dets=0">here!</a>');
       |         $$("#userDetails").html('to login: User: nimrodl@wix.com , Password: GNK5OdwkZzh5Qw7f9qPB');
       |       }).fail(function(error) {
       |             $$("#resultText").html('Sorry! timeout contacting amplitude service. checkout results <a href="https://amplitude.com/app/151746/funnels?fid=20206&sset=%7B%22byProp%22:%22a%22,%22segmentIndex%22:0%7D&sset=%7B%22byProp%22:%22b%22,%22segmentIndex%22:0%7D&cg=User&range=Last%207%20Days&i=1&dets=0">here!</a>');
       |             $$("#userDetails").html('to login: User: nimrodl@wix.com , Password: GNK5OdwkZzh5Qw7f9qPB');
       |         })
       |     });
       |});
       |</script>
       |</html>
    """.stripMargin
  }

  def colorFromExperiment() =
    laboratory.conductExperiment("BUTTON_COLOR_SPEC", "yellow", new StringConverter)

  @RequestMapping(value = Array("/buttonClicked"), method = Array(RequestMethod.POST))
  @ResponseBody
  def buttonClicked(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    getUserId(request).foreach { userId =>
      amplitudeAdapter.sendEvent(AmplitudeEvent(ButtonClickedEvent.eventType, "1.1.1.1", "en", "us", userId))
    }
  }

  private def getOrPutUserId(request: HttpServletRequest, response: HttpServletResponse): String = {
    val userId = getUserId(request).getOrElse(UUID.randomUUID().toString)
    response.addCookie(new Cookie(labUserIdField, userId))
    userId
  }

  private def getUserId(request: HttpServletRequest) = {
    val cookie = request.getCookies.toSet.find(_.getName == labUserIdField).map(_.getValue)
    Option(request.getParameter(labUserIdField)).orElse(cookie)
  }
}

object ButtonClickedEvent {
  val eventType = "ButtonClickedEvent"
}

object PageWithButtonController {
  val labUserIdField = "laboratory_user_id"
}

case class AmplitudeEvent(@JsonProperty("event_type") eventType: String,
                          ip: String, language: String, country: String,
                          @JsonProperty("user_id") userId: String) extends BaseAmplitudeEvent