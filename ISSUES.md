# M1

## List of issues

### Issue 1: [Collect update-profile functions]

**Description**:[Before there were several large functions for updating different user parameters. There was one function for updating the bio and name of the profile and one function for updating the hobbies of a profile]

**How it was fixed?**: [I wrote the functions together in on function called updateProfile() taking any amount of user parameters and leaving the other null so they dont get overwritten. This is also good for future implementations so that we dont have to write a new update function for every new parameter the user might get.]

### Issue 2: [LoadProfile() dependant on both user and hobbies]

**Description**:[To be able to load the profile, we indeed need the user, but as of now, we also need the hobbies otherwise it throws an error. It would be nice if we could get the user even if there was an error with the hobbies.]

**How it was fixed?**: [I discovered this issue as i tried to implement the feature with languages spoken. I fixed it by removing the condition that the profile retreival AND hobbies retreival had to be successes and just threw an error instead if any of them were failures. This is however still not great and I would like to split them up single functions as not to mix up the responsibilty of the function.]

### Issue 3: [WRITE_ISSUE_TITLE]

...
