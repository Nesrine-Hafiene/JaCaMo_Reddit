// Common code for the organisational part of the system

task_roles("submit_post",  [reddit_contributor]).
task_roles("retrieve_post",  [reddit_subscriber]).


!in_ora4mas.
!in_bh_institution.

+!contract(Task,GroupName)
    : task_roles(Task,Roles)
   <- !in_bh_institution; //the hired agent becomes part of the institution.
      !in_ora4mas;
      .print("Contract ",Task, GroupName );
      lookupArtifact(GroupName, GroupId);
      for ( .member( Role, Roles) ) {
         focus(GroupId);
      }
      .

-!contract(Service,GroupName)[error(E),error_msg(Msg),code(Cmd),code_src(Src),code_line(Line)]
   <- .print("Failed to sign the contract for ",Service,"/",GroupName,": ",Msg," (",E,"). command: ",Cmd, " on ",Src,":", Line).


+!in_ora4mas
   <- joinWorkspace("/main/ora4mas",_);
      lookupArtifact("reddit_group", GroupId);
      focus(GroupId).
      

-!in_ora4mas
   <- .wait(200); 
      !in_ora4mas.


+!in_bh_institution <-
   joinWorkspace("/main/bh",_);
   lookupArtifact("bh_art", InstArt);
   focus(InstArt).
   


-!in_bh_institution <-       
   .wait(100);
   !in_bh_institution.


//if the clock has been started, do nothing.
+!start_institutional_clock : nticks(N) & N>0.

+!start_institutional_clock 
   <- joinWorkspace("/main/wsp_auction",_);
      lookupArtifact("clock", ClockId);
      focus(ClockId);
      start.

-!start_institutional_clock 
   <- .wait(100);
      !start_institutional_clock.



{ include("$jacamoJar/templates/common-moise.asl") }
{ include("$jacamoJar/templates/org-obedient.asl") }
