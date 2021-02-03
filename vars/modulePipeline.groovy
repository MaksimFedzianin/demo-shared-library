def call(body){
	inputParams = [:]
	
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = inputParams
	body()
	
	
	node {
		stage('Checkout') {
			git(
					url          : inputParams.repoUrl,
					branch       : inputParams.gitBranch
				)
		}
		stage('Build'){
			bat 'gradlew clean build -x test'
		}
		stage('Test'){
			bat 'gradlew test'
		}
		if(inputParams.gitBranch.equals("master")){
			stage('Deploy'){
				rtServer (
					id: "local-artifactory",
					url: "http://localhost:8081/artifactory",
					credentialsId: inputParams.artifactoryCredentialsId
				)
				
				rtUpload(
					serverId: "local-artifactory",
					spec: """
						"files" : [
							{
								"pattern" : "build/libs/*.jar",
								"target" : "test-repo/my-builds/$inputParams.gitBranch/${BUILD_NUMBER}"
							}
						]
					"""
				)
			}
		}
	}
	
}