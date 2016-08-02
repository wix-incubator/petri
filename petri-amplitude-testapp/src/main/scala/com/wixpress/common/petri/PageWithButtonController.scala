package com.wixpress.common.petri

import java.util.UUID
import javax.annotation.Resource
import javax.servlet.http.{HttpServletRequest, HttpSession}

import com.wixpress.petri.laboratory.converters.StringConverter
import com.wixpress.petri.laboratory.{AmplitudeAdapter, AmplitudeEvent, Laboratory}
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
  def test(request: HttpServletRequest): String = {
    getOrPutUserId(request)
    val color = colorFromExperiment()

    s"""
       |<html>
       |<head>
       |<script src="https://code.jquery.com/jquery-1.9.1.js"></script>
       |</head>
       |<body>
       |<div>
       |  <input type="button" name="buttonId" id="buttonId" value="Click here!" style="color: $color"/>
       |</div>
       |<div id="resultText">
       |</div>
       |</body>
       |<script>
       |$$(document).ready(function() {
       |             $$("#resultText").html("b4");
       |    $$("#buttonId").click(function() {
       |       $$.post( "/buttonClicked", function(res) {
       |         $$("#resultText").html("ok");
       |       }).fail(function(error) {
       |             $$("#resultText").html("failed");
       |         })
       |     });
       |});
       |</script>
       |</html>
    """.stripMargin
  }

  def colorFromExperiment() = {
    laboratory.conductExperiment("BUTTON_COLOR_SPEC", "yellow", new StringConverter) match {
      case "a" => "red"
      case "b" => "blue"
      case _ => "yellow"
    }
  }

  @RequestMapping(value = Array("/buttonClicked"), method = Array(RequestMethod.POST))
  @ResponseBody
  def buttonClicked(request: HttpServletRequest): Unit = {
    val userId = getOrPutUserId(request)

    amplitudeAdapter.sendEvent(AmplitudeEvent(ButtonClickedEvent.eventType, "1.1.1.1", "en", "us", userId))
  }

  private def getOrPutUserId(request: HttpServletRequest): String = {
    val labUserIdField = "laboratory_user_id"

    def getUserId(request: HttpServletRequest, session: HttpSession) =
      Option(request.getParameter(labUserIdField)).orElse(Option(session.getAttribute(labUserIdField))).map(_.toString)

    val session = request.getSession
    val userId = getUserId(request, session).getOrElse(UUID.randomUUID().toString)
    session.setAttribute(labUserIdField, userId)
    userId
  }
}

object ButtonClickedEvent {
  val eventType = "ButtonClickedEvent"
}