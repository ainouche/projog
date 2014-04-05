x(A,B,C) :- C=A+B.
x(A,B,C) :- C=A-B.
x(A,B,C) :- C=A*B.
x(A,B,C) :- C=A/B.

a(A,B,C) :- x(A,B,D), C is D.

b(A,C) :- a(A,A,C).

% %QUERY% b(5,A)
% %ANSWER% A=10
% %ANSWER% A=0
% %ANSWER% A=25
% %ANSWER% A=1

% %QUERY% b(26/4-1,A)
% %ANSWER% A=10
% %ANSWER% A=0
% %ANSWER% A=25
% %ANSWER% A=1

% %QUERY% b(5.5,A)
% %ANSWER% A=11.0
% %ANSWER% A=0.0
% %ANSWER% A=30.25
% %ANSWER% A=1.0

% %QUERY% b(-(3*7.5),A)
% %ANSWER% A=-45.0
% %ANSWER% A=0.0
% %ANSWER% A=506.25
% %ANSWER% A=1.0

% %QUERY% a(9,3,A)
% %ANSWER% A=12
% %ANSWER% A=6
% %ANSWER% A=27
% %ANSWER% A=3

% %QUERY% a(10,2.5,A)
% %ANSWER% A=12.5
% %ANSWER% A=7.5
% %ANSWER% A=25.0
% %ANSWER% A=4.0

% %QUERY% a(25.5,12,A)
% %ANSWER% A=37.5
% %ANSWER% A=13.5
% %ANSWER% A=306.0
% %ANSWER% A=2.125