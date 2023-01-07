
Initial starting code (arch document): YWtxLWiUzblw
Code VM startup: NYzCloHClYru
Code from the VM: kPMVcpKxeHeb
Code from the tablet: KbrjQwEtVdiK
Code after teleporter: YDffGaaFFhsM

[Solution to the game](https://github.com/fwenzel/synacor-challenge/blob/master/vm/solution.sh)


Details on the binary:
String are being preceded by an integer indicating their length

Memory locations:
memory offset 6068 start of static data (strings)
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


## subroutine 1767-1840 READ STRING FROM IN
On the call, register <a> contains the max string length, and register <b> contains the address where
to store the string. Reading end when a newline character is received.
On return, the first byte of the target address, contains the length of the string received. 