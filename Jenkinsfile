@Library('defra-shared') _
@Library('data-returns-ci')

def buildProperties
node {
    buildProperties = buildConfiguration('rcr_api.groovy')
}

pipeline {
    agent { label buildProperties['jenkins.slave'] }
    stages {
        stage('Build project artifacts') {
            steps {
                sh 'printenv'
                sh "./mvnw -B -DskipTests -Ddependency-check.skip=true -DskipTests=true -Dcheckstyle.skip=true -T 1C clean package"
            }
        }
        stage('Build docker image') {
            steps {
                sh "./mvnw dockerfile:build"
            }
        }
        stage('Push docker image') {
            steps {
                script {
                    docker.withRegistry(buildProperties['ecr.registry.url'], buildProperties['ecr.registry.credentials']) {
                        docker.image(buildProperties['ecr.repository.name'] + ':latest').push(generateBuildTag())
                    }
                }
            }
        }
        stage("Cleanup") {
            steps {
                cleanWs cleanWhenFailure: false
            }
        }
    }
}
