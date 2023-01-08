# [Synacor challenge](https://challenge.synacor.com/)

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

The Synacore challenge has much similarity with the Intcode device from 2019. I didnt reuse any code and
started this one from scratch. Afterwards I've compared the two solutions, and found many similarities
in my design and implementation, but differences as well.

The addition of the keyboard to pass commands was new, and of course for AoC 2019, no Debugger was 
required. All in all, by no means a perfect design this time, but much better compared to my 2019 
implementation of the Intcode device.

I do need to apologize for the lack of unit tests ... I never mastered the discipline of TDD, and 
because I took many small incremental steps during design and implementation, I didn't feel an 
urgent need for unit tests either