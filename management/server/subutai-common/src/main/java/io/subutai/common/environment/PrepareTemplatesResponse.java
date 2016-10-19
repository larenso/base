package io.subutai.common.environment;


import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.util.HostUtil;


public class PrepareTemplatesResponse
{
    private Set<String> messages = Sets.newHashSet();
    private boolean hasSucceeded;


    public void addResults( HostUtil.Results results )
    {
        Preconditions.checkNotNull( results );

        for ( HostUtil.Task task : results.getTasks().getTasks() )
        {
            if ( task.getTaskState() == HostUtil.Task.TaskState.SUCCEEDED )
            {
                this.messages.add( String
                        .format( "Task (%s) succeeded on host %s [%s]", task.name(), task.getHost().getHostname(),
                                task.getDurationFormatted() ) );
            }
            else if ( task.getTaskState() == HostUtil.Task.TaskState.FAILED )
            {
                this.messages.add( String
                        .format( "Task (%s) failed on host %s [%s]", task.name(), task.getHost().getHostname(),
                                task.getFailureReason() ) );
            }
        }

        this.hasSucceeded = !results.hasFailures();
    }


    public Set<String> getMessages()
    {
        return messages;
    }


    public boolean hasSucceeded()
    {
        return hasSucceeded;
    }
}
