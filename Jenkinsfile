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
                sh "./gradlew jacocoTestCoverageVerification"
            }

        }

    }
}
