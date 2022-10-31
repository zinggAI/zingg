import requests
import json
import time
 
class ZinggDatabricksJob:
   
  name = None
  id = None
  url = None
  _headers = None
  
  
  def __init__(self, name, databricks_workspace_url, api_token):  
    
    # attribute assignments
    self.name = name
    self.url = databricks_workspace_url
    self._headers = {'Authorization': f'Bearer {api_token}'}
    
    # get job id (based on job name)
    self.id = self._get_job_id()
    if self.id is None:
      self = None # invalidate self
      raise ValueError(f"A job with the name '{name}' was not found.  Please create the required jobs before attempting to proceed.")
    
  def _get_job_id(self):
    
    job_id = None
    
    # get list of jobs in databricks workspace
    job_resp = requests.get(f'https://{self.url}/api/2.0/jobs/list', headers=self._headers)
    
    # find job by name
    for job in job_resp.json().get('jobs'):
        if job.get('settings').get('name')==self.name:
            job_id = job.get('job_id') 
            break
    return job_id
  
  def run(self):
    post_body = {'job_id': self.id}
    run_resp = requests.post(f'https://{self.url}/api/2.0/jobs/run-now', json=post_body, headers=self._headers)
    run_id = run_resp.json().get('run_id')
    return run_id
  
  def wait_for_completion(self, run_id):
    
    # seconds to sleep between checks
    sleep_seconds = 30
    start_time = time.time()
    
    # loop indefinitely
    while True:
      
      # retrieve job info
      resp = requests.get(f'https://{self.url}/api/2.0/jobs/runs/get?run_id={run_id}', headers=self._headers)
      
      #calculate elapsed seconds
      elapsed_seconds = int(time.time()-start_time)
      
      # get job lfe cycle state
      life_cycle_state = resp.json().get('state').get('life_cycle_state')
      
      # if terminated, then get result state & break loop
      if life_cycle_state == 'TERMINATED':
          result_state = resp.json().get('state').get('result_state')
          break
          
      # else, report to user and sleep
      else:
          if elapsed_seconds > 0:
            print(f'Job in {life_cycle_state} state at { elapsed_seconds } seconds since launch.  Waiting {sleep_seconds} seconds before checking again.', end='\r')
          
          time.sleep(sleep_seconds)
    
    # return results
    print(f'Job completed in {result_state} state after { elapsed_seconds } seconds.  Please proceed with next steps to process the records identified by the job.')
    print('\n')         
         
    return result_state
 
  def run_and_wait(self):
    return self.wait_for_completion(self.run())
  