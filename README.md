# jenkins-shared-lib

Jenkins Shared Library extracted from the `Ivolve-DevOps-Jenkins-Pipeline` pipeline.

## Folder structure

```
vars/
├── buildApp.groovy
├── buildAndPushImage.groovy
└── deployToK8s.groovy
```

## Configure in Jenkins

In **Manage Jenkins -> Configure System -> Global Pipeline Libraries**, add:

- Name: `jenkins-shared-lib`
- Default version: `main`
- Retrieval method: Modern SCM -> Git -> `https://github.com/MostafaOsmanFathi/jenkins-shared-lib.git`

## Usage in a Jenkinsfile

```groovy
@Library('jenkins-shared-lib') _

pipeline {
    agent any
    environment {
        DOCKER_IMAGE = 'mostafaosmanfathi/ivolve-nodejs-app'
    }
    stages {
        stage('Build app') { steps { buildApp() } }
        stage('Build and push image') { steps { buildAndPushImage() } }
        stage('Deploy to K8s') { steps { deployToK8s() } }
    }
}
```

Each step accepts optional parameters (image, tag, namespace, credentialsId, ...).
See the header comments in each file under `vars/`.
```
