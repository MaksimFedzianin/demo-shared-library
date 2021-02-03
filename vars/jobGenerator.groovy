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
			echo branches
		}
	}
}
