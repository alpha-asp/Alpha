motive(a).
motive(b).
guilty(b).

% ei incumbit probatio qui dicit, non qui negat
innocent(S) :- motive(S), not guilty(S).

% Evaluating:
%
% Run the following command, substituting the name of the JAR file and the path
% to this file as required:
%
%    java -jar alpha-bundled.jar -i .../innocence.asp
