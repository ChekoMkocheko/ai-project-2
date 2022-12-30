Cheko Mkocheko and Alivia Kliesen

Names of Files:
    SudokuPlayer.java

Known Bugs:

    AC3 algorithm has no known bugs. (passed all given tests)

    Custom solver can't solve the given medium or difficult Sudoku puzzles. 

Customsolver:

    Our custom solver method involves performing an AC3 search with 
    forward tracking instead of backtracking. In forward tracking, one
    looks at the other variables affected by a potential assignment and then updates
    their domains accordingly (backtracking does not update other affected
    variables' domains). If a neighbor variable's domain becomes empty, the
    forward tracking algorithm is able to catch this early and then tries
    different a assignment for the cell from its domain (sooner than backtracking is usually able
    to catch on).

    Our custom solver is only able to solve the easy Sudoku puzzle. It is
    not able to ever finish running on the medium or difficult Sudoku example
    puzzles.

    Given that our custom solver cannot solve medium or difficult Sudoku puzzles,
    it has a worse performance than the AC3 solver. We think the issue with the Custom
    Solver is the way it stores and updates previous domains (if down the line of
    recursively forward tracking it realizes that a previous assignment made was invalid).
    We think if one recursive call is made it is able to correct the previous assignment,
    but if multiple incorrect recursive calls are made, it has difficult recovering (which 
    is why it fails on the medium and difficult Sudoku puzzles). Perhaps some sort of 
    queue could be used to store previous domains, but we were unsure how to implement
    this with the time we had to complete this assignment.