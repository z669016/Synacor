# [Synacore challenge](https://challenge.synacor.com/)

Parts of the puzzle I didn't like that much. Analyzing byte code just isn't my thing. However, 
writing the VM and implementing a debugger to step through code and update the code while running
was a real joy.

I heavily reused the solution by others to do the more detailed hacking of the code, so credits to 
Fred and Gabriel for cracking it! Maybe I'll solve the vault-challenge and the Ackermann 
variant at a later stage (but I don't think so).

[Solution by Fred Wenzel](https://github.com/fwenzel/synacor-challenge/blob/master/vm/solution.sh)

[Solution by Gabriel Kanegae](https://github.com/KanegaeGabriel/synacor-challenge)

What I did learn on the code:
String are being preceded by an integer indicating their length

Memory locations:
- memory offset 06068 start of static data (strings)
- memory offset 25974 is used as a commandline buffer
- memory offset 27398 contains an array of 7 pointers to integers containing address of commands
- memory offset 25943 start of list of commands (static strings)
- memory offset 27407 list of subroutines for internal commands (go, look, help, inv, take, drop, use)
- memory offset 18063 list off possible items in your inventory and the message to show when you "look" at it

Gathered codes along the way:
- Initial starting code (arch document): YWtxLWiUzblw
- Code VM startup: NYzCloHClYru
- Code from the VM: kPMVcpKxeHeb
- Code from the tablet: KbrjQwEtVdiK
- Code in the room of the can: juBgvqxEDmZk
- Code after using teleporter the first time: YDffGaaFFhsM
- Code after successful teleportation: WQXztEwCdxHW
- Code in the mirror: AudlVOilwdlO --> OlbwliOVlbuA (mirrored, so backwards and flip d to b)


 