// Shared library step: updateArgoCD
// After an image has been pushed, this step updates the image tag in the
// GitOps repository (IVolveArgoCD) and pushes the change, so ArgoCD can
// reconcile the new image into the cluster.
//
// Usage:
//   updateArgoCD()
//   updateArgoCD(image: 'org/app', tag: env.BUILD_NUMBER, gitCredentialsId: 'Github')
def call(Map config = [:]) {
    def image = config.image ?: env.DOCKER_IMAGE ?: 'mostafaosmanfathi/ivolve-nodejs-app'
    def tag = config.tag ?: env.BUILD_NUMBER ?: 'latest'
    def fullImage = "${image}:${tag}"
    def gitOpsRepoUrl = config.gitOpsRepoUrl ?: 'git@github.com:MostafaOsmanFathi/IVolveArgoCD.git'
    def gitOpsBranch = config.gitOpsBranch ?: 'main'
    def manifestPath = config.manifestPath ?: 'ArgoCD/deployment.yaml'
    def gitCredentialsId = config.gitCredentialsId ?: 'Github'
    def workDir = config.workDir ?: "ivolve-argocd-${tag}"

    echo "======== Update GitOps manifest for ArgoCD ========"
    sh "rm -rf ${workDir}"

    sshagent(credentials: [gitCredentialsId]) {
        withEnv(['GIT_SSH_COMMAND=ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null']) {
            sh "git clone ${gitOpsRepoUrl} ${workDir}"
        }
    }

    sh "sed -i 's|${image}:.*|${fullImage}|' ${workDir}/${manifestPath}"

    sshagent(credentials: [gitCredentialsId]) {
        withEnv(['GIT_SSH_COMMAND=ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null']) {
            sh """
            cd ${workDir}
            git config user.email "jenkins@ivolve.local"
            git config user.name "Jenkins"
            git add ${manifestPath}
            git commit -m "chore: update image to ${fullImage}" || echo "No changes to commit"
            git push origin ${gitOpsBranch}
            """
        }
    }

    sh "rm -rf ${workDir}"
    echo "GitOps manifest updated to ${fullImage}; ArgoCD will reconcile."
}
