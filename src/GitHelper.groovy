class GitHelper {

	private final Script script

	GitHelper(Script script){
		this.script = script
	}
		

	List getBranches(){
		
		String remoteBranches = script.bat(returnStdout : true, script : '@echo off | git branch -a | findstr \"remotes/origin\"')
		remoteBranches = remoteBranches.replace("renites/origin/", "")
		
		List result = []
		
		remoteBranches.readLines().each { line ->
			result.add(line.trim())
		}
		
		return result
	}
}