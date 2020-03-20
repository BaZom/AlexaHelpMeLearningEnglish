/**
    Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package com.amazon.customskill;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SsmlOutputSpeech;

/*
 * This class is the actual skill. Here you receive the input and have to produce the speech output. 
 * for more details about this class see @link https://github.com/arun-gupta/alexa-skills-kit-java/blob/master/src/com/amazon/speech/speechlet/SpeechletV2.java
 */
public class AlexaSkillSpeechlet implements SpeechletV2 {
	static Logger logger = LoggerFactory.getLogger(AlexaSkillSpeechlet.class);
	public static String userRequest;
	private Question question;
	private Text text;

	private static enum RecognitionState {
		Answer, YesNo, TestOrText, YesNoText, YesNoQuestion, YesNotipp, YesNoTest
	};

	private static enum UserIntent {
		Yes, No, A, B, C, D, text, test, easy, middle, hard, challenging, question, no, leave
	};

	private RecognitionState recState;
	private String testQuestion;
	private String AnswerTestYes;
	private String AnswerTestNo;
	private int testQuestionCounter = 1;
	boolean newText = false;
	boolean newQuestion = false;
	boolean TippAgain = false;
	int counter = 1;
	int counterQuestions = 1;
	int next = 0;
	UserIntent ourUserIntent;
	private String theText;
	private String level;
	private int TextID;
	private String Question;
	static String welcomeMsg = "Hello. I will enjoy learning English with you! I will read you different texts and ask a few questions afterward. If you give the right answer, we will jump to the next question. There are 3 questions for each text.\r\n"
			+ "If your answer is incorrect, you can decide if you want to listen to the text again or if you want me to give you a hint. \r\n"
			+ "If you want to end the application, please say: stop application.\r\n"
			+ "Shall we begin? What level of difficulty should your texts have? Easy, Middle, Hard or Challenging?\r\n"
			+ " You are not sure what your level is? If you say: “Test”, I will tell you how fluent you are.";
	static String wrongMsg = "I'm sorry, but that was incorrect. Should I give you a hint?";
	static String correctMsg = "Great! Your answer is right! Do want to listen again? yes or no ?";
	static String hearAgain = "do you want to hear it again?";
	static String goodbyeMsg = "I look forward for your next lesson  bye!";
	static String errorYesNoMsg = "I did not understand that. Please say yes or no.";
	static String errorAnswerMsg = "I did not understand that. Say your answer again please.";

	/**
	 * selects a text using Text class based on the level of difficulty. TextID is
	 * used later to help determining which question we need from the database.
	 */

	public void selectText(String l) {

		switch (l) {

		case "a1": {

			text = new Text("a1");
			theText = text.selectText();
			TextID = text.getTextID();
			level = "a1";
			break;

		}

		case "a2": {

			Text text = new Text("a2");
			theText = text.selectText();
			TextID = text.getTextID();
			break;
		}
		case "b1": {

			Text text = new Text("b1");
			theText = text.selectText();
			TextID = text.getTextID();
			break;
		}
		case "b2": {

			Text text = new Text("b2");
			theText = text.selectText();
			TextID = text.getTextID();
			break;
		}

		default:

		}

	}

	/*******************************************************************************************************************************************************************************/

	@Override
	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
		logger.info("Alexa session begins");
		recState = RecognitionState.Answer;
	}

	/*******************************************************************************************************************************************************************************/

	@Override
	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
		return askUserResponseExtras(welcomeMsg, 0);
	}

	/*******************************************************************************************************************************************************************************/

	@Override
	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
		IntentRequest request = requestEnvelope.getRequest();
		Intent intent = request.getIntent();
		userRequest = intent.getSlot("anything").getValue();
		logger.info("Received following text: [" + userRequest + "]");
		logger.info("recState is [" + recState + "]");
		SpeechletResponse resp = null;
		// handeling different intents of the user.
		switch (recState) {
		case Answer: {
			resp = evaluateAnswer(userRequest);
			break;
		}
		case YesNoText: {
			resp = evaluateYesNoText(userRequest);
			break;
		}
		case YesNoQuestion: {
			resp = evaluateYesNoQuestion(userRequest);
			break;
		}

		case YesNo: {
			resp = evaluateYesNo(userRequest);
			break;
		}
		case YesNotipp: {
			resp = evaluateYesNotipp(userRequest);
			break;
		}
		case YesNoTest: {
			resp = evaluateYesNoTest(userRequest);
			break;
		}
		default: {
			resp = response("Erkannter Text: " + userRequest);
			break;
		}
		}
		return resp;
	}

	/**
	 * evaluates the answer of the user in cases of yes-no-answer situations.
	 * 
	 **/

	private SpeechletResponse evaluateYesNo(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case Yes: {

			selectText(level);
			newText = true;
			res = askUserResponseText("", theText, newText);
			recState = RecognitionState.YesNoText;

			break;
		}
		case No: {
			res = askUserResponseExtras(goodbyeMsg, 0);
			break;
		}
		case leave: {
			res = askUserResponseExtras(goodbyeMsg, 0);
			break;
		}
		default: {
			res = askUserResponseExtras(errorYesNoMsg, 0);
		}
		}
		return res;
	}

	/**
	 * evaluates the answer of the user in cases of yes-no-answer situations for a
	 * question. in case of answer yes, Alexa reads the same question again.
	 **/
	private SpeechletResponse evaluateYesNoQuestion(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case Yes: {
			newQuestion = true;
			res = askUserResponseQuestion(Question, newQuestion);
			break;
		}
		case No: {
			res = askUserResponseExtras("what is your Answer then ? ", 1);
			recState = RecognitionState.Answer;

			break;
		}
		case leave: {
			res = askUserResponseExtras(goodbyeMsg, 0);
			break;
		}
		default: {
			res = askUserResponseExtras(errorYesNoMsg, 0);
		}
		}
		return res;
	}

	/**
	 * evaluates the answer of the user in cases of yes-no-answer situations for a
	 * text. in case of Answer No, it selects a question from the database using
	 * Question class. in case of answer yes, Alexa reads the same text again.
	 * 
	 **/
	private SpeechletResponse evaluateYesNoText(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case Yes: {
			res = askUserResponseText("", theText, newText);
			break;
		}
		case No: {

			question = new Question(TextID);
			question.rand = 1;
			Question = question.selectQuestion();
			res = askUserResponseQuestion(Question, newQuestion);
			recState = RecognitionState.Answer;
			break;
		}
		case leave: {
			res = askUserResponseExtras(goodbyeMsg, 0);
			break;
		}
		default: {
			res = askUserResponseExtras(errorYesNoMsg, 0);
		}
		}
		return res;
	}

	/**
	 * evaluates the answer of the user in cases of yes-no-answer situations for a
	 * tip. in case of answer yes, Alexa reads a tip from the current question
	 * object.
	 **/
	private SpeechletResponse evaluateYesNotipp(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case Yes: {
			TippAgain = true;
			res = askUserResponse(" ok here is your tip   " + question.getTipp());
			recState = RecognitionState.Answer;
			break;
		}
		case No: {

			res = askUserResponseExtras("Try to say the answer again then ", 1);
			recState = RecognitionState.Answer;
			break;
		}
		case leave: {
			res = askUserResponseExtras(goodbyeMsg, 0);
			break;
		}
		default: {
			res = askUserResponseExtras(errorYesNoMsg, 0);
		}
		}
		return res;
	}

	/**
	 * evaluates the answer of the user in cases of different situations.
	 **/
	private SpeechletResponse evaluateAnswer(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {

		// a question is selected from the database using class Question and with help
		// of the current TextID
		case question: {
			if (question.rand < 3) {
				question.rand++;
				Question = question.selectQuestion();
				res = askUserResponseQuestion(Question, newQuestion);
				recState = RecognitionState.Answer;
			} else {
				ResponseOutOfQuestion();
				selectText(level);
			}

			break;
		}

		// a text is selected from the database using class Text and with help of the
		// level.
		case text: {
			selectText(level);
			res = askUserResponseText("", theText, newText);
			recState = RecognitionState.YesNoText;
			break;
		}

		/*
		 * for the cases easy, middle, hard and challenging, a text is selected from the
		 * Database using the Text class and the level of difficulty is passed based on
		 * each case
		 */
		case easy: {
			selectText("a1");
			res = askUserResponseText("", theText, newText);
			recState = RecognitionState.YesNoText;
			break;
		}
		case middle: {

			selectText("a2");
			res = askUserResponseText("", theText, newText);
			recState = RecognitionState.YesNoText;

			break;
		}
		case hard: {

			selectText("b1");
			res = askUserResponseText("", theText, newText);
			recState = RecognitionState.YesNoText;

			break;
		}
		case challenging: {

			selectText("b2");
			res = askUserResponseText("", theText, newText);
			recState = RecognitionState.YesNoText;

			break;

		}

		// user can leave the app anytime by saying leave.
		case leave: {
			res = askUserResponseExtras(goodbyeMsg, 0);
			break;
		}

		// a test will begin when the user decide at the beginning that he wants to make
		// a test.
		case test: {
			makeTest(1);
			res = askUserResponseTest(0);
			recState = RecognitionState.YesNoTest;

			break;

		}

		/*
		 * This part is responsible for evaluating the user answers for the questions.
		 * if the answer is correct, the next question is read (up to three questions
		 * pro text). If the answer is incorrect, the user can ask for a tip. after two
		 * times wrong answers, the user should decide if he/she would like to have
		 * another question or another text.
		 */
		default: {
			if (ourUserIntent.equals(UserIntent.A) || ourUserIntent.equals(UserIntent.B)
					|| ourUserIntent.equals(UserIntent.C) || ourUserIntent.equals(UserIntent.D)) {
				logger.info("User answer =" + ourUserIntent.name().toLowerCase() + "/correct answer="
						+ question.getCorrectAnswer());
				if (ourUserIntent.name().toLowerCase().equals(question.getCorrectAnswer())) {

					counterQuestions++;
					if (counterQuestions < 4) {
						next++;
						res = askUserResponseExtras(
								question.getAlexaCorrectAnswer() + "let's listen to the next question", next);

						recState = RecognitionState.Answer;
					} else {
						counterQuestions = 1;
						theText = text.selectText();
						TextID = text.getTextID();
						newText = true;
						res = askUserResponseText("", theText, newText);
						recState = RecognitionState.YesNoText;
					}

					counter = 1;
				} else {

					if (counter == 2) {
						res = askUserResponseExtras(
								" Your answer was incorrect again. Would you like to hear another question or another text? say question for a new question or text for a new text. leave to exit ",
								0);
						recState = RecognitionState.Answer;
						counter = 1;

					} else if (counter == 1) {
						counter = 2;
						res = askUserResponseExtras(wrongMsg, 0);
						recState = RecognitionState.YesNotipp;
					}

				}
			} else {
				res = askUserResponseExtras(errorAnswerMsg, 0);
			}
			break;
		}
		}
		return res;
	}

	/**
	 * evaluates the answer of the user in cases of yes-no-answer situations for a
	 * test. in case of answer yes, Alexa reads the next question of the test except
	 * the last question where a text of the highest level is read for the user. in
	 * case of answer no, Alexa decides the level of the user based on his last yes
	 * answer.
	 **/
	private SpeechletResponse evaluateYesNoTest(String userRequest) {
		SpeechletResponse res = null;
		recognizeUserIntent(userRequest);
		switch (ourUserIntent) {
		case Yes: {
			if (testQuestionCounter == 4) {
				selectText(level);
				res = askUserResponseText(AnswerTestYes, theText, newText);
			} else {
				testQuestionCounter++;
				makeTest(testQuestionCounter);
				res = askUserResponseTest(testQuestionCounter);
			}
			break;
		}
		case No: {
			if (testQuestionCounter == 1) {
				res = askUserResponseTest(10);
			} else if (testQuestionCounter <= 4) {
				makeTest(testQuestionCounter);
				selectText(level);
				res = askUserResponseText(AnswerTestNo, theText, newText);
				recState = RecognitionState.YesNoText;
				testQuestionCounter = 1;
			}
			break;
		}
		case leave: {
			res = askUserResponseExtras(goodbyeMsg, 0);
			break;
		}
		default: {
			res = askUserResponseExtras(errorYesNoMsg, 0);
		}
		}
		return res;
	}

	/*
	 * a so called test to help the user to determine his/her level of difficulty.
	 */
	private void makeTest(int nextTestQuestion) {

		switch (nextTestQuestion) {
		case 1: {
			testQuestion = "Can you introduce yourself and tell me where you live?";
			AnswerTestNo = "you should learn some basics before you return to this app. This might a too advanced for you.";
			level = "a1";
			break;
		}
		case 2: {
			testQuestion = "Great. Are you able to tell in detils how your day was?";
			AnswerTestNo = "ok. I will play some easy texts for you then. Lets start";
			level = "a2";
			break;
		}
		case 3: {
			testQuestion = " Very good. Are you also able to describe things around you or other objects?";
			AnswerTestNo = "ok. I will play some suitable texts for you. Lets start!";
			level = "b1";
			break;
		}
		case 4: {
			testQuestion = "Okay, last question. Can you give details about how the past is connected with the future?";
			AnswerTestYes = "Wow, your English is very good. You will go to our challenging level, let’s start!";
			AnswerTestNo = "okay. You will get some hard texts though becuase you seem to be very good in English. Lets start!";
			level = "b2";
			break;
		}

		}
	}

	/*
	 * A function helps to give a more room for the users answers. Example: instead
	 * of saying text, the user can say "a text please"
	 */
	void recognizeUserIntent(String userRequest) {
		userRequest = userRequest.toLowerCase();

		String pattern1 = "(I take )?(answer )?(\\b[a-d]\\b)( please)?";
		String pattern2 = "question";
		String pattern3 = "(I take )?(level )?(middle)(please)?";
		String pattern4 = "\\byes\\b";
		String pattern5 = "\\bno\\b";
		String pattern6 = "(I take )?(a )?(text)( please)?";
		String pattern7 = "(I take )?(level )?(easy)(please)?";
		String pattern8 = "(I take )?(level )?(hard)(please)?";
		String pattern9 = "(I take )?(level )?(challenging)(please)?";
		String pattern10 = "leave";
		String pattern11 = "(I take )?(a )?(test)( please)?";
		String pattern12 = "yes";
		String pattern13 = "no";
		Pattern p1 = Pattern.compile(pattern1);
		Matcher m1 = p1.matcher(userRequest);
		Pattern p2 = Pattern.compile(pattern2);
		Matcher m2 = p2.matcher(userRequest);
		Pattern p3 = Pattern.compile(pattern3);
		Matcher m3 = p3.matcher(userRequest);
		Pattern p4 = Pattern.compile(pattern4);
		Matcher m4 = p4.matcher(userRequest);
		Pattern p5 = Pattern.compile(pattern5);
		Matcher m5 = p5.matcher(userRequest);
		Pattern p6 = Pattern.compile(pattern6);
		Matcher m6 = p6.matcher(userRequest);
		Pattern p7 = Pattern.compile(pattern7);
		Matcher m7 = p7.matcher(userRequest);
		Pattern p8 = Pattern.compile(pattern8);
		Matcher m8 = p8.matcher(userRequest);
		Pattern p9 = Pattern.compile(pattern9);
		Matcher m9 = p9.matcher(userRequest);
		Pattern p10 = Pattern.compile(pattern10);
		Matcher m10 = p10.matcher(userRequest);
		Pattern p11 = Pattern.compile(pattern11);
		Matcher m11 = p11.matcher(userRequest);
		Pattern p12 = Pattern.compile(pattern12);
		Matcher m12 = p12.matcher(userRequest);
		Pattern p13 = Pattern.compile(pattern13);
		Matcher m13 = p13.matcher(userRequest);
		if (m1.find()) {
			String answer = m1.group(3);
			switch (answer) {
			case "a":
				ourUserIntent = UserIntent.A;
				break;
			case "b":
				ourUserIntent = UserIntent.B;
				break;
			case "c":
				ourUserIntent = UserIntent.C;
				break;
			case "d":
				ourUserIntent = UserIntent.D;
				break;
			}
		} else if (m2.find()) {

			ourUserIntent = UserIntent.question;

		} else if (m3.find()) {
			ourUserIntent = UserIntent.middle;

		} else if (m4.find() || m12.find()) {
			ourUserIntent = UserIntent.Yes;
		} else if (m5.find() || m13.find()) {
			ourUserIntent = UserIntent.No;
		} else if (m6.find()) {
			ourUserIntent = UserIntent.text;

		} else if (m7.find()) {
			ourUserIntent = UserIntent.easy;
		} else if (m8.find()) {
			ourUserIntent = UserIntent.hard;
		} else if (m9.find()) {
			ourUserIntent = UserIntent.challenging;
		} else if (m10.find()) {
			ourUserIntent = UserIntent.leave;
		} else if (m11.find()) {
			ourUserIntent = UserIntent.test;
		}
		logger.info("set ourUserIntent to " + ourUserIntent);

	}

	/*******************************************************************************************************************************************************************************/

	@Override
	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
		logger.info("Alexa session ends now");
	}

	/*******************************************************************************************************************************************************************************/

	private SpeechletResponse response(String text) {
		// Create the plain text output.
		PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
		speech.setText(text);

		return SpeechletResponse.newTellResponse(speech);
	}

	/*
	 * the coming three functions manage how alexa the questions reads and responses
	 * 
	 */
	private SpeechletResponse askUserResponseExtras(String text, int next) {
		SsmlOutputSpeech speech = new SsmlOutputSpeech();

		if (next == 1) {
			question = new Question(TextID);
			if (question.rand == 2) {
				question.rand = 3;
			} else {
				question.rand = 2;
			}
			Question = question.selectQuestion();

			speech.setSsml(
					"<speak>" + text + "  <audio src=\"soundbank://soundlibrary/alarms/beeps_and_bloops/bell_02\"/> "
							+ Question + "</speak>");

		} else if (next == 2) {
			question = new Question(TextID);
			if (question.rand == 3) {
				ResponseOutOfQuestion();
				selectText(level);
			} else {
				question.rand = 3;
			}
			Question = question.selectQuestion();

			speech.setSsml(
					"<speak>" + text + "  <audio src=\"soundbank://soundlibrary/alarms/beeps_and_bloops/bell_02\"/> "
							+ Question + "</speak>");

		} else {
			speech.setSsml("<speak>" + text + "</speak>");

		}

		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
		repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> you still there?</speak>");

		Reprompt rep = new Reprompt();
		rep.setOutputSpeech(repromptSpeech);

		return SpeechletResponse.newAskResponse(speech, rep);
	}

	/*******************************************************************************************************************************************************************************/

	private SpeechletResponse askUserResponseQuestion(String question, boolean newQuestion) {
		SsmlOutputSpeech speech = new SsmlOutputSpeech();

		if (newQuestion == true) {
			speech.setSsml("<speak> ok here is your question again " + question
					+ "<audio src=\\\"soundbank://soundlibrary/alarms/beeps_and_bloops/bell_02\\\"/> </speak>");
			newQuestion = false;
		} else {
			speech.setSsml(
					"<speak> here is your question <audio src=\"soundbank://soundlibrary/alarms/beeps_and_bloops/bell_02\"/>"
							+ question + "</speak>");
		}
		// reprompt after 8 seconds
		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
		repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> you still there?</speak>");

		Reprompt rep = new Reprompt();
		rep.setOutputSpeech(repromptSpeech);

		return SpeechletResponse.newAskResponse(speech, rep);
	}

	/*******************************************************************************************************************************************************************************/

	private SpeechletResponse ResponseOutOfQuestion() {
		SsmlOutputSpeech speech = new SsmlOutputSpeech();

		speech.setSsml("<speak> We ran out of questions for this Text you will get a new Text instead </speak>");

		// reprompt after 8 seconds
		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
		repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> you still there?</speak>");

		Reprompt rep = new Reprompt();
		rep.setOutputSpeech(repromptSpeech);

		return SpeechletResponse.newAskResponse(speech, rep);
	}

	/*
	 * the coming two functions manage how alexa the texts reads and responses
	 * 
	 */
	private SpeechletResponse askUserResponse(String text) {
		SsmlOutputSpeech speech = new SsmlOutputSpeech();

		speech.setSsml("<speak>" + text + "  <audio src=\"soundbank://soundlibrary/alarms/beeps_and_bloops/bell_02\"/> "
				+ Question + "</speak>");

		// reprompt after 8 seconds
		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
		repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> you still there?</speak>");

		Reprompt rep = new Reprompt();
		rep.setOutputSpeech(repromptSpeech);

		return SpeechletResponse.newAskResponse(speech, rep);
	}

	/*******************************************************************************************************************************************************************************/

	private SpeechletResponse askUserResponseText(String Extra, String text, boolean nText) {
		SsmlOutputSpeech speech = new SsmlOutputSpeech();

		if (nText == true) {
			speech.setSsml(
					"<speak> you got all answers right Let's begin once more. The Text starts after the tone <audio src=\"soundbank://soundlibrary/alarms/beeps_and_bloops/bell_02\"/> <voice name=\"Brian\">"
							+ text + " </voice> That was your text " + hearAgain + "</speak>");
			newText = false;
		} else {
			speech.setSsml("<speak>" + Extra
					+ ". Your Text starts after the tone <audio src=\"soundbank://soundlibrary/alarms/beeps_and_bloops/bell_02\"/> <voice name=\"Brian\">"
					+ text + " </voice>  That was your text " + hearAgain + "</speak>");
		}

		// reprompt after 8 seconds
		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
		repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> you still there?</speak>");

		Reprompt rep = new Reprompt();
		rep.setOutputSpeech(repromptSpeech);

		return SpeechletResponse.newAskResponse(speech, rep);
	}

	/*
	 * this function manages how alexa the test questions reads and responses
	 * 
	 * */
	private SpeechletResponse askUserResponseTest(int x) {
		SsmlOutputSpeech speech = new SsmlOutputSpeech();

		if (x == 0) {
			speech.setSsml(
					"<speak>Please note, that this test is just a short oral version and won't determine your level a 100%. If you want to be sure what your level is, please go to cambridgeenglish.org to test your knowledge in a written test. "
							+ "I will ask you a few questions to determine your level of language knowledge. "
							+ "Please answer with yes or no. Let’s GO! " + testQuestion + "</speak>");
		} else if (x > 0 && x < 5) {
			speech.setSsml("<speak>" + testQuestion + "</speak>");
		} else if (x == 5) {

			speech.setSsml("<speak>" + AnswerTestYes + "</speak>");
			selectText(level);

		} else if (x == 10) {
			speech.setSsml("<speak>" + AnswerTestNo + "</speak>");

		}

		// reprompt after 8 seconds
		SsmlOutputSpeech repromptSpeech = new SsmlOutputSpeech();
		repromptSpeech.setSsml("<speak><emphasis level=\"strong\">Hey!</emphasis> you still there?</speak>");

		Reprompt rep = new Reprompt();
		rep.setOutputSpeech(repromptSpeech);

		return SpeechletResponse.newAskResponse(speech, rep);

	}

}
