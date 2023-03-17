def buildImage(ecr_url, app_name, app_version) {
    sh """
        docker build -t ${ecr_url}/${app_name}:${app_version} ."
    """
}

def pushImage(ecr_url, app_name, app_version) {
    sh """
        docker push ${ecr_url}/${app_name}:${app_version}"
    """
}

def ecr_login(region, String awscli = "aws") {
    sh """
    ${awscli} ecr get-login --no-include-email --region "${region}"
    """
}

def deploy(cluster, service, task_family, image, region, boolean is_wait = true, String awscli = "aws") {
    sh """
        OLD_TASK_DEF=\$(${awscli} ecs describe-task-definition \
                                --task-definition ${task_family} \
                                --output json --region ${region})
        NEW_TASK_DEF=\$(echo \$OLD_TASK_DEF | \
                    jq --arg NDI ${image} '.taskDefinition.containerDefinitions[0].image=\$NDI')
        FINAL_TASK=\$(echo \$NEW_TASK_DEF | \
                    jq '.taskDefinition | \
                            {family: .family, \
                            networkMode: .networkMode, \
                            volumes: .volumes, \
                            containerDefinitions: .containerDefinitions, \
                            placementConstraints: .placementConstraints}')
        ${awscli} ecs register-task-definition \
                --family ${task_family} \
                --cli-input-json \
                "\$(echo \$FINAL_TASK)" --region "${region}"
        if [ \$? -eq 0 ]
        then
            echo "New task has been registered"
        else
            echo "Error in task registration"
            exit 1
        fi
        
        echo "Now deploying new version..."
                    
        ${awscli} ecs update-service \
            --cluster ${cluster} \
            --service ${service} \
            --force-new-deployment \
            --task-definition ${task_family} \
            --region "${region}"
        
        if ${is_wait}; then
            echo "Waiting for deployment to reflect changes"
            ${awscli} ecs wait services-stable \
                --cluster ${cluster} \
                --service ${service} \
                --region "${region}"
        fi
    """
}
