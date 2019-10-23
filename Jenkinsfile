pipeline{
    agent any
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
                sh "./gradlew testClasses"
            }
        }
        stage("Code coverage"){
            steps{
                sh "./gradlew jacocoTestReport"
				publishHTML	(target:	[
					reportDir: 'build/reports/jacoco/test/html',
					reportFiles: 'index.html',
					reportName:	"JaCoCo	Report"
				])
                //sh "./gradlew jacocoTestCoverageVerification"
            }

        }

    }
}
