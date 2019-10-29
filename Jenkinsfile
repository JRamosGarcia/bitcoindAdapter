pipeline{
    agent any
//    triggers {
//         pollSCM('H * * * *')
//     }
    stages{
        stage("Checkout"){
            steps{
                git url: 'https://github.com/JRamosGarcia/bitcoindAdapter.git'
            }
        }
        stage("Compile"){
            steps {
                sh "./gradlew classes"
            }
        }
        stage("Unit tests"){
            steps{
                sh "./gradlew test"
            }
        }
        stage("Code coverage"){
            steps{
                sh "./gradlew jacocoTestReport"
				publishHTML	(target:	[
					reportDir: 'build/reports/jacoco/test/html',
					reportFiles:'index.html',
					reportName: "JaCoCo	Report"
				])
				//TODO: No pasamos los test de cobertura.
                //sh "./gradlew jacocoTestCoverageVerification"
            }
        }
        stage("Static code analysis"){
			steps{
				sh "./gradlew checkstyleMain --stacktrace"
				publishHTML	(target:	[
					reportDir: 'build/reports/checkstyle/',
					reportFiles: 'main.html',
					reportName: "Checkstyle	Report"
				])
			}
		}
		stage("Package"){
			steps{
		    	sh "./gradlew bootJar"
			}
		}
		stage("Docker build"){
			steps{
		    	sh "docker build -t localhost:5000/bitcoind_adapter:${BUILD_TIMESTAMP} ."
			}
		}
		stage("Docker push"){
		    steps{
		        sh "docker push localhost:5000/bitcoind_adapter:${BUILD_TIMESTAMP}"
		    }
		}
		stage("Deploy to Staging"){
			steps{
			    sh "docker-compose up -d"
			}
		}
		stage("Acceptance Test"){
			steps{
			    sleep 100
			    sh "./acceptanceTest.sh"
			}			
		}
	}	
	post{
		    always{
		        sh "docker-compose down"
		    }

	}
}
