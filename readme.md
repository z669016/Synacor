
Initial starting code (arch document): YWtxLWiUzblw
Code VM startup: NYzCloHClYru
Code from the VM: kPMVcpKxeHeb
Code from the tablet: KbrjQwEtVdiK
Code after teleporter: YDffGaaFFhsM
Code after successful teleportation: WQXztEwCdxHW

[Solution to the game](https://github.com/fwenzel/synacor-challenge/blob/master/vm/solution.sh)


Details on the binary:
String are being preceded by an integer indicating their length

Memory locations:
memory offset 06068 start of static data (strings)
memory offset 25974 is used as an commandline buffer
memory offset 27398 contains an array of 7 pointers to integers containing address of commands
memory offset 25943 start of list of commands (static strings)
memory offset 27407 list of subroutines for internal commands (go, look, help, inv, take, drop, use)
memory offset 18063 list off possible items in your inventory and the message to show when you "look" at it

## start of program
Starting at address 00000, welcome message including 1st progress code 'NYzCloHClYru'. The welcome message is 
output using output instruction for the individual characters, so no reference to a static data area in
memory. No subroutines being used yet, as it's not sure if they will work (I guess).

## Self test
The self test runs from address 00272, with a message and a JMP. If the JMP is not properly executed, an
error message is shown followed by a HALT instruction. form 00272 until 00484 JMP instructions are tested
(incl. off-by-one errors).
00563 test ADD
00564 test EQ
00633 test bitwise AND
00644 test bitwise OR
00684 test bitwise NOT
00704 test CALL
00747 test modulo on ADD
00779 test MULT
00808 test MOD
00843 reserved
00844 reserved
00845 test RMEM/WMEM, using memory address 00843 and 00844
01023 End of self test, prints message that self test is complete

The area up to XXXX contains error messages from the self test, all ending with a HALT instruction.

The fact that the test modulo on ADD is near the test of MULT and not with the initial ADD test, suggests
this test and arch feature was added later.

After the self-test, the static string in memory get decrypted using a routine at 02125

## start of the main program 02734 ???

## sub 02125

## sub 01458-01517 (STRING <a>).FOREACH <b>
For each character on the string, perform <b>. The address of the string is in register <a>, the address of the 
subroutine is in the <b> register. On return, the <b> register contains the index of the last character processed.

## sub 01518-01527 (STRING <a>).OUT
Output a string from memory, the address of the string is in the <a> register. Uses FOREACH to write the string 
to the out port of the device passing subroutine 01528 as the address for the write operation.

## sub 01528-01530 (CHAR <a>).OUT
Output the contents of register <a>

## sub 01531-01542 output a single character with manipulation
Performs AND <c> <a> <b>, NOT <c> <c>, OR <a> <a> <b>, AND <a> <a> <c>, before output of the character

## sub 01543-1570 helper routine for (STRING <a>).FOREACH (PREDICATE <b> with <c>)
Test all characters of a string for a predicate (e.g. equality with a certain value). On return <a> will contain 
the index of the character that passes the predicate test, of a high value (32767) if it didn't anywhere.

## sub 01571-01587 (STRING <a>).INDEX_OF CHAR <b>
Find character <b> in the string referred to by <a>

## sub 01588-01604 (String <a>).INDEX_OF(STRING REF TABLE <b>)
Find the string <a> in a ref table of strings in <b>. In the end <b> will contain the index of the
matched string. Used for instance to identify a command (by looking the command in the command table)
or to find an item in your inventory for a "use" command.

## sub 01605-01618 LAMBDA (CHAR <a>).EQ CHAR <c>
Compares the values of <a> and <c>, when equal keep <b> in <c> and set <b> to high value (32767), which will cause 
the FOREACH to stop.

## sub 01619-01647 LAMBDA (CHAR <a>).EQ((STRING <c>[<b> + 1])) 
Compare character in <a> (passed in by the FOREACH) with character [b+1] of the string in <c>. When
not equal set <c> to the index of the mismatch, and set <b> to high value (32767) to end the FOREACH.

## sub 01648-1666 (STRING <a>).EQ(String <c>) then <b>
Compare string <a> with <c> and when equal return the value of <b> (an index) and set <b> to high value
(32767) to end a foreach.

## sub 01667-01722 (STRING <a>).EQ(String <b>)
Compare two strings for equality, and set <a> to 1 when equal and to 0 if not.

## sub 1767-1840 READ STRING FROM IN
On the call, register <a> contains the max string length, and register <b> contains the address where
to store the string. Reading end when a newline character is received.
On return, the first byte of the target address, contains the length of the string received. 