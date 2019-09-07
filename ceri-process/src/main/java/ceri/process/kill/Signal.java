package ceri.process.kill;

public enum Signal {
    HUP(1),   // hang up
    INT(2),   // interrupt
    QUIT(3),  // quit
    ABRT(6),  // abort
    KILL(9),  // non-catchable, non-ignorable kill
    ALRM(14), // alarm clock
    TERM(15); // software termination signal (default)
    
    public final int number;
    
    Signal(int number) {
    	this.number = number;
    }
    
}
