import GitHelper

def call(body) {
	
	inputParams = [:]
	
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = inputParams
	body()
	
	echo 'job generator from demo library'
	
	
	
	node {
		stage('Pull Source'){
			checkout scm: [$class: 'GitSCM',
									   userRemoteConfigs: [[url: inputParams.repoUrl]]
						]
		}
		
		stage('Update Module Jobs'){
			List branches = new GitHelper(this).getBranches()
			echo branches.toString()
			
			jobDslExecute("""
				folder('module'){
					description 'Forlder for module pipelines'
					displayName '$inputParams.modulePipelineName'
				}
			""")
			
			branches.each { branch ->
				String jobName = "module/" + branch.replace("/", "_")
				
				String scriptText = """
					modulePipeline {
						gitBranch = "$branch"
						repoUrl = "$inputParams.repoUrl"
						artifactoryCredentialsId = "$inputParams.artifactoryCredentialsId"
					}
				"""
				
				jobDslExecute("""
					pipelineJob('$jobName') {
						description '\"$jobName\" pipeline'
													
						definition {
							cps {
								script(\"\"\"$scriptText\"\"\")
								sandbox(true)
							}						
						}
							
						logRotator {
							numToKeep(5)
						}
							
					}		
				""")
				
			}
		}
		
		stage('Run Module Jobs'){
			try {
				jenkins.model.Jenkins.instance.getAllItems(Job.class).each{

					if (!it.fullName.startsWith('module/')) {
						return;
					}

					if (it.getBuilds().size() == 0) {
						println("Starting job $it.fullName")

						build job: it.fullName, quietPeriod: 0, wait: false;
					}

				}
			} catch (err) {
				echo "failed to run job : $err"
			}
		}
	}
}

private void jobDslExecute(String jobDslScript) {
	echo jobDslScript
	jobDsl scriptText: jobDslScript
}
