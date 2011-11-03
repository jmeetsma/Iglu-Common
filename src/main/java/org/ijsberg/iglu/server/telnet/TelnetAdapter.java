package org.ijsberg.iglu.server.telnet;

import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.server.connection.CommandLineClientAdapter;
import org.ijsberg.iglu.server.connection.CommandLineInterpreter;
import org.ijsberg.iglu.server.connection.Connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * This class translates communication between a telnet client and an interpreter.
 * Incoming bytes are used to put together a command line.
 * The line is regarded complete if a NEWLINE character is encountered.
 * It contains some features to enhance usability, such as:
 * <ul>
 * <li>a hide mode (echo off) for input of passwords</li>
 * <li>a streaming mode for pushing data</li>
 * <li>interception of special characters, such as ctrl-c</li>
 * <li>use of cursor keys</li>
 * <li>use of backspace</li>
 * <li>command line completion</li>
 * <li>prompt support</li>
 * </ul>
 * The command lines are interpreted and processed by a commandline interpreter.
 */
public class TelnetAdapter implements CommandLineClientAdapter {
	private OutputStream os;
	private PrintStream ps;
	//buffer to store command line input
	private ByteArrayOutputStream commandLineBuffer;
	//buffer to store control character input
	private byte[] controlCharBuffer = new byte[4];
	//echo characters by default
	private boolean echoEnabled = true;

	private CommandLineInterpreter interpreter;
	private boolean adapterTerminating = false;

	/*
	List of ASCII characters

	0-31 non-printing
	32-126 printing
	127 DEL / BACK

	000      000    000   00000000      NUL    (Null char.)
	001      001    001   00000001      SOH    (Start of Header)
	002      002    002   00000010      STX    (Start of Text)
	003      003    003   00000011      ETX    (End of Text)
	004      004    004   00000100      EOT    (End of Transmission)
	005      005    005   00000101      ENQ    (Enquiry)
	006      006    006   00000110      ACK    (Acknowledgment)
	007      007    007   00000111      BEL    (Bell)
	008      010    008   00001000       BS    (Backspace)
	009      011    009   00001001       HT    (Horizontal Tab)
	010      012    00A   00001010       LF    (Line Feed)
	011      013    00B   00001011       VT    (Vertical Tab)
	012      014    00C   00001100       FF    (StandardForm Feed)
	013      015    00D   00001101       CR    (Carriage Return)
	014      016    00E   00001110       SO    (Shift Out)
	015      017    00F   00001111       SI    (Shift In)
	016      020    010   00010000      DLE    (Data Link Escape)
	017      021    011   00010001      DC1 (XON) (Device Control 1)
	018      022    012   00010010      DC2       (Device Control 2)
	019      023    013   00010011      DC3 (XOFF)(Device Control 3)
	020      024    014   00010100      DC4       (Device Control 4)
	021      025    015   00010101      NAK    (Negative Acknowledgement)
	022      026    016   00010110      SYN    (Synchronous Idle)
	023      027    017   00010111      ETB    (End of Trans. Block)
	024      030    018   00011000      CAN    (Cancel)
	025      031    019   00011001       EM    (End of Medium)
	026      032    01A   00011010      SUB    (Substitute)
	027      033    01B   00011011      ESC    (Escape)
	028      034    01C   00011100       FS    (File Separator)
	029      035    01D   00011101       GS    (Group Separator)
	030      036    01E   00011110       RS    (Request to Send)(MonitoredUnitRecord Separator)
	031      037    01F   00011111       US    (Unit Separator)
	032      040    020   00100000       SP    (Space)
	033      041    021   00100001        !    (exclamation mark)
	034      042    022   00100010        "    (double quote)
	035      043    023   00100011        #    (number sign)
	036      044    024   00100100        $    (dollar sign)
	037      045    025   00100101        %    (percent)
	038      046    026   00100110        &    (ampersand)
	039      047    027   00100111        '    (single quote)
	040      050    028   00101000        (    (left/opening parenthesis)
	041      051    029   00101001        )    (right/closing parenthesis)
	042      052    02A   00101010        *    (asterisk)
	043      053    02B   00101011        +    (plus)
	044      054    02C   00101100        ,    (comma)
	045      055    02D   00101101        -    (minus or dash)
	046      056    02E   00101110        .    (dot)
	047      057    02F   00101111        /    (forward slash)
	048      060    030   00110000        0
	049      061    031   00110001        1
	050      062    032   00110010        2
	051      063    033   00110011        3
	052      064    034   00110100        4
	053      065    035   00110101        5
	054      066    036   00110110        6
	055      067    037   00110111        7
	056      070    038   00111000        8
	057      071    039   00111001        9
	058      072    03A   00111010        :    (colon)
	059      073    03B   00111011        ;    (semi-colon)
	060      074    03C   00111100        <    (less than)
	061      075    03D   00111101        =    (equal sign)
	062      076    03E   00111110        >    (greater than)
	063      077    03F   00111111        ?    (question mark)
	064      100    040   01000000        @    (AT symbol)
	065      101    041   01000001        A
	066      102    042   01000010        B
	067      103    043   01000011        C
	068      104    044   01000100        D
	069      105    045   01000101        E
	070      106    046   01000110        F
	071      107    047   01000111        G
	072      110    048   01001000        H
	073      111    049   01001001        I
	074      112    04A   01001010        J
	075      113    04B   01001011        K
	076      114    04C   01001100        L
	077      115    04D   01001101        M
	078      116    04E   01001110        N
	079      117    04F   01001111        O
	080      120    050   01010000        P
	081      121    051   01010001        Q
	082      122    052   01010010        R
	083      123    053   01010011        S
	084      124    054   01010100        T
	085      125    055   01010101        U
	086      126    056   01010110        V
	087      127    057   01010111        W
	088      130    058   01011000        X
	089      131    059   01011001        Y
	090      132    05A   01011010        Z
	091      133    05B   01011011        [    (left/opening bracket)
	092      134    05C   01011100        \    (back slash)
	093      135    05D   01011101        ]    (right/closing bracket)
	094      136    05E   01011110        ^    (caret/cirumflex)
	095      137    05F   01011111        _    (underscore)
	096      140    060   01100000        `
	097      141    061   01100001        a
	098      142    062   01100010        b
	099      143    063   01100011        c
	100      144    064   01100100        d
	101      145    065   01100101        e
	102      146    066   01100110        f
	103      147    067   01100111        g
	104      150    068   01101000        h
	105      151    069   01101001        i
	106      152    06A   01101010        j
	107      153    06B   01101011        k
	108      154    06C   01101100        l
	109      155    06D   01101101        m
	110      156    06E   01101110        n
	111      157    06F   01101111        o
	112      160    070   01110000        p
	113      161    071   01110001        q
	114      162    072   01110010        r
	115      163    073   01110011        s
	116      164    074   01110100        t
	117      165    075   01110101        u
	118      166    076   01110110        v
	119      167    077   01110111        w
	120      170    078   01111000        x
	121      171    079   01111001        y
	122      172    07A   01111010        z
	123      173    07B   01111011        {    (left/opening brace)
	124      174    07C   01111100        |    (vertical bar)
	125      175    07D   01111101        }    (right/closing brace)
	126      176    07E   01111110        ~    (tilde)
	127      177    07F   01111111      DEL    (delete)

	IAC 255		 interpret as command: 
	DONT 254	 you are not to use option 
	DO 253		 please, you use option 
	WONT 252	 I won't use option 
	WILL 251	 I will use option 
	SB 250		 interpret as subnegotiation 
	GA 249		 you may reverse the line 
	EL 248		 erase the current line 
	EC 247		 erase the current character 
	AYT 246		 are you there 
	AO 245		 abort output--but let prog finish 
	IP 244		 interrupt process--permanently 
	BREAK 243	 break 
	DM 242		 data mark--for connect. cleaning 
	NOP 241		 nop 
	SE 240		 end sub negotiation 
	EOR 239		 end of record (transparent mode) 
	ABORT 238	 Abort process 
	SUSP 237	 Suspend process 
	xEOF 236	 End of file: EOF is already used...
	*/

	public static final byte CTRLC = 3;
	public static final byte BEL = 7;
	public static final byte BACK = 8;
	public static final byte TAB = 9;
	public static final byte NEWLINE = 10;
	public static final byte RETURN = 13;

	public static final byte ESC = 27;

	public static final byte DEL = 127;

	public static final byte IAC = (byte) 255;

	public static final byte[] UP = {27, 91, 65, 0};
	public static final byte[] DOWN = {27, 91, 66, 0};
	public static final byte[] RIGHT = {27, 91, 67, 0};
	public static final byte[] LEFT = {27, 91, 68, 0};

	public static final byte[] HOME = {27, 91, 49, 126};
	public static final byte[] INSERT = {27, 91, 50, 126};
	public static final byte[] DELETE = {27, 91, 51, 126};
	public static final byte[] END = {27, 91, 52, 126};
	public static final byte[] PGUP = {27, 91, 53, 126};
	public static final byte[] PGDOWN = {27, 91, 54, 126};


	private int HISTORY_SIZE = 25;
	private int controlCharIndex;
	//indicates read of control character (sequence)
	private boolean controlCharCaptureMode;
	private String promptStr = "";
	private int historyIndex;
	private ArrayList history = new ArrayList(10);
	private int curPos;
	private Connection connection;


	/**
	 * Default constructor for easy instantiation.
	 */
	public TelnetAdapter() {
	}

	/**
	 * @param connection
	 * @param os
	 * @param interpreter
	 * @throws IOException
	 */
	public void initiateSession(Connection connection, OutputStream os, CommandLineInterpreter interpreter) throws IOException {
		this.connection = connection;
		this.interpreter = interpreter;

		this.os = os;
		ps = new PrintStream(os);

		commandLineBuffer = new ByteArrayOutputStream();

		//negotiate behaviour
		//enable character-at-a-time mode (IAC WILL ECHO, IAC WILL SUPPRESS-GO-AHEAD, IAC)
		os.write(new byte[]{(byte) 255, (byte) 251, (byte) 1, (byte) 255, (byte) 251, (byte) 3/*, (byte) 255, (byte) 253, (byte) 243*/});
		interpreter.initiateSession(this/*, agentId*/);
	}

	/**
	 * Contains the loop that processes incoming bytes.
	 */
	public void receive(byte[] byteArray) throws IOException {
		//register the current application for this thread
		// in case a subsystem logs to the environment
		String result;
		if (interpreter.isInSubProcessMode()) {
			if (!abortSubprocessModeIfNecessary(byteArray)) {
				/*			case CTRLC:
				   interpreter.abortSubprocessMode();
				   break;*/
				if (echoEnabled) {
					echoBytesToClient(byteArray);
				}
				//every byte is forwarded because the subprocess will interprete the input
				interpreter.processRawInput(byteArray);
			}
		}
		else {
			processInput(byteArray);
		}
	}

	/**
	 * @param rawInput
	 * @return true if subprocessmode is actually aborted
	 */
	private boolean abortSubprocessModeIfNecessary(byte[] rawInput) {
		for (int i = 0; i < rawInput.length; i++) {
			if (rawInput[i] == CTRLC) {
				//the telnet adapter takes initiative
				interpreter.abortSubProcessMode();
				return true;
			}
		}
		return false;
	}


	private void processInput(byte[] byteArray)
			throws IOException {
		String result;
		for (int i = 0; i < byteArray.length; i++) {
			byte b2 = byteArray[i];
			if (!controlCharCaptureMode) {
				processCommandlineCharacter(b2);
			}
			else {
				processControlCharacter(b2);
			}
		}
	}

	private void processCommandlineCharacter(byte b2)
			throws IOException {

		switch (b2) {
			case ESC:
				switchToControlCharCaptureMode(b2);
				break;
			case IAC:
				switchToControlCharCaptureMode(b2);
				break;
			case TAB:
				replaceCommandLine(interpreter.completeCommand(commandLineBuffer.toString()));
				curPos = commandLineBuffer.size();
				break;
			case RETURN:
				//write/echo new line
				handleReturn();
				break;
				//read all characters except returns
			case BACK:
				//the back key in case of an MS-DOS telnet client
				handleBackKey();
				break;
			case DEL://the back key in case of an MS-DOS telnet client
				/*
				 //enable for MS-DOS telnet clients if they can be detected
				 if (curPos < commandLineBuffer.size())
				 {
					 handleDeleteKey();
				 }
				 else
				*/
				handleBackKey();
				break;
			default: {
				//NEWLINE is ignored
				if (b2 > 31) {
					processPrintableCharacter(b2);
				}
				break;
			}
		}
	}

	private void handleReturn()
			throws IOException {
		String result;
		ps.write(new byte[]{RETURN, NEWLINE});

		//line completed
		String line = commandLineBuffer.toString();
		if (echoEnabled && !"".equals(line)) {
			//if echo is disabled (for instance because of password entry)
			//  the line will not be archived
			storeCommandlineInHistory(line);
			curPos = 0;
		}
		//process commandline
		try {
			result = interpreter.processCommandLine(line).replaceAll("\n", "\r\n");
		}
		catch (Exception e)//interpreter is not trusted
		{
			result = "command line could not be interpreted due to exception " + e.getClass().getName() + (e.getMessage() != null ? " with message " + e.getMessage() : "");
		}

		if (result != null && result.length() > 0) {
			ps.print(result);
			char prompt = result.charAt(result.length() - 1);
			promptStr = result;
			if (prompt == RETURN || prompt == NEWLINE) {
				promptStr = "";
			}
			else if (result.indexOf(RETURN) != -1) {
				promptStr = result.substring(result.lastIndexOf(RETURN) + 2);
			}
		}
		//empty buffer
		commandLineBuffer.reset();
	}

	private void storeCommandlineInHistory(String line) {
		history.remove("");
		if (history.contains(line)) {
			history.remove(line);
		}
		if (history.size() >= HISTORY_SIZE) {
			history.remove(history.size() - 1);
		}
		history.add(0, line);
		history.add(0, "");
		historyIndex = 0;
	}

	private void switchToControlCharCaptureMode(byte b2) {
		controlCharCaptureMode = true;
		controlCharBuffer[0] = b2;
		controlCharIndex = 1;
	}

	private void processPrintableCharacter(byte b2) {
		//echo character to client
		if (echoEnabled) {
			ps.write(b2);
		}

		if (curPos < commandLineBuffer.size()) {
			insertCharacterInCommandlineBuffer(b2);
		}
		else {
			//store input
			commandLineBuffer.write(b2);
			curPos++;
		}
	}

	private void insertCharacterInCommandlineBuffer(byte b2) {
		String curLine = commandLineBuffer.toString();
		commandLineBuffer.reset();
		for (int x = 0; x < curLine.length(); x++) {
			if (x == curPos) {
				commandLineBuffer.write(b2);
			}
			commandLineBuffer.write(curLine.charAt(x));
			if (x >= curPos) {
				if (echoEnabled) {
					ps.write(curLine.charAt(x));
				}
			}
		}
		for (int x = curPos; x < curLine.length(); x++) {
			if (echoEnabled) {
				ps.write(BACK);
			}
		}
		curPos++;
	}

	private void processControlCharacter(byte b2)
			throws IOException {
		//controlCharMode
		controlCharBuffer[controlCharIndex++] = b2;
		//some keys send 3 characters, others 4
		if (controlCharIndex == 3 && controlCharBuffer[0] == IAC) {
			disableControlCharCaptureMode();
		}
		else if ((controlCharIndex == 3 && b2 > 64) || controlCharIndex == 4) {
			handleControlCharSequence();
		}
	}

	private void handleControlCharSequence() throws IOException {
		String controlStr = new String(controlCharBuffer);
		if (controlStr.equals(new String(UP)) && history.size() > 1 && echoEnabled) {
			showNextCommandlineFromHistory();
		}
		else if (controlStr.equals(new String(DOWN)) && history.size() > 1 && echoEnabled) {
			showPreviousCommandlineFromHistory();
		}
		else if (controlStr.equals(new String(LEFT)) && echoEnabled) {
			moveCursorToLeft();
		}
		else if (controlStr.equals(new String(RIGHT)) && echoEnabled) {
			moveCursorToRight();
		}
		else if (controlStr.equals(new String(HOME)) && echoEnabled) {
			moveCursorToLineStart();
		}
		else if (controlStr.equals(new String(END)) && echoEnabled) {
			moveCursorToLineEnd();
		}
		else if (controlStr.equals(new String(DELETE)) && echoEnabled) {
			handleDeleteKey();
		}
		else if (controlStr.equals(new String(PGUP)) && echoEnabled) {
			//									os.write(1);//SOH
			//									os.write(2);//SOT
			//									os.write(11);//VT
			//									os.write(12);//FF
		}
		disableControlCharCaptureMode();
	}

	private void moveCursorToLineEnd() {
		byte[] clbuf = commandLineBuffer.toByteArray();
		for (int x = curPos; x < commandLineBuffer.size(); x++) {
			ps.write(clbuf[x]);
		}
		curPos = commandLineBuffer.size();
	}

	private void moveCursorToLineStart() {
		for (int x = curPos; x > 0; x--) {
			ps.write(BACK);
		}
		curPos = 0;
	}

	private void moveCursorToRight() {
		if (curPos < commandLineBuffer.size()) {
			//move to right by resending character at current position
			ps.write(commandLineBuffer.toByteArray()[curPos]);
			curPos++;
		}
	}

	private void moveCursorToLeft() {
		if (curPos > 0) {
			curPos--;
			ps.write(BACK);
		}
	}

	private void showPreviousCommandlineFromHistory() {
		if (historyIndex > 0) {
			historyIndex--;
		}
		replaceCommandLine((String) history.get(historyIndex));
		curPos = commandLineBuffer.size();
	}

	private void showNextCommandlineFromHistory() {
		if (historyIndex < history.size() - 1) {
			historyIndex++;
		}
		replaceCommandLine((String) history.get(historyIndex));
		curPos = commandLineBuffer.size();
	}

	private void disableControlCharCaptureMode() {
		controlCharCaptureMode = false;
		controlCharIndex = 0;
		controlCharBuffer[3] = 0;
	}

	private void echoBytesToClient(byte[] b) {
		for (int i = 0; i < b.length; i++) {
			ps.write(b[i]);
		}
	}

	/**
	 * Replaces the current command line on the telnet client with another one.
	 *
	 * @param replacement
	 */
	private void replaceCommandLine(String replacement) {
		int currentCLSize = commandLineBuffer.size();
		commandLineBuffer = new ByteArrayOutputStream();
		for (int x = 0; x < replacement.length(); x++) {
			commandLineBuffer.write(replacement.charAt(x));
		}
		String replaceString = '\r' + promptStr;
		for (int x = 0; x <= currentCLSize; x++) {
			replaceString += ' ';
		}
		replaceString += '\r' + promptStr + replacement;
		ps.print(replaceString);
	}


	/**
	 * Stops echoing characters in the telnet client.
	 */
	public void disableEcho() {
		echoEnabled = false;
	}


	/**
	 * Starts echoing characters in the telnet client.
	 */
	public void enableEcho() {
		echoEnabled = true;
	}


	/**
	 * Sends a message to the telnet client.
	 *
	 * @param message message
	 */
	public synchronized void send(String message) {
		ps.print(message);
	}


	/**
	 * Sends a message to the telnet client.
	 *
	 * @param message message
	 */
	public void send(byte[] message) {
		try {
			ps.write(message);
		}
		catch (IOException ioe) {
			System.out.println(new LogEntry(Level.CRITICAL, "IO exception occurred while writing to client", ioe));
			connection.close("IO exception occurred while writing to client");
		}
	}




	/**
	 * Tells the client to make a sound.
	 */
	public void beep() {
		try {
			os.write(BEL);
		}
		catch (IOException ioe) {
			System.out.println(new LogEntry(Level.CRITICAL, "IO exception occurred while writing to client", ioe));
			connection.close("IO exception occurred while writing to client");
		}
	}

	public void onConnectionClose(String message) {
		interpreter.onAdapterTermination(message);
	}


	/**
	 */
	public void terminateSession() {
		connection.close();
	}



	/**
	 * Rewrites the command line to show changes as a result of hitting the back key
	 * even if the cursor is positioned in the middle of the command line
	 *
	 * @throws IOException
	 */
	private void handleBackKey() throws IOException {
		if (commandLineBuffer.size() > 0 && curPos > 0) {
			if (echoEnabled) {
				ps.write(BACK);
			}
			String curLine = overwriteCommandlineSkippingChar(curPos - 1);
			eraseLastCharAndMoveBack(curLine, curPos);
			curPos--;
		}
	}


	/**
	 * Rewrites the command line to show changes as a result of hitting the delete key
	 * when the cursor is positioned in the middle of the command line
	 *
	 * @throws IOException
	 */
	private void handleDeleteKey() throws IOException {
		if (commandLineBuffer.size() > 0 && curPos < commandLineBuffer.size()) {
			String curLine = overwriteCommandlineSkippingChar(curPos);
			eraseLastCharAndMoveBack(curLine, curPos + 1);
		}
	}

	private void eraseLastCharAndMoveBack(String curLine, int curPos) throws IOException {
		if (echoEnabled) {
			ps.write(new byte[]{' ', BACK});
			for (int x = curPos; x < curLine.length(); x++) {
				ps.write(BACK);
			}
		}
	}

	private String overwriteCommandlineSkippingChar(int curPos) {
		String curLine = commandLineBuffer.toString();
		commandLineBuffer.reset();
		int len = curLine.length() - 1;
		for (int x = 0; x < len; x++) {
			if (x == (curPos)) {
				//skip unwanted character
				x++;
				//last character is not removed, so it must be written
				len++;
			}
			commandLineBuffer.write(curLine.charAt(x));
			if (x >= curPos) {
				if (echoEnabled) {
					ps.write(curLine.charAt(x));
				}
			}
		}
		return curLine;
	}
}
