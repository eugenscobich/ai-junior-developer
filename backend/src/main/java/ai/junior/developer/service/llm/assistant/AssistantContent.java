package ai.junior.developer.service.llm.assistant;

public class AssistantContent {

    public final static String ASSISTANT_MODEL = "gpt-4.1";
    public final static String ASSISTANT_NAME = "AI Junior Developer";
    public final static String ASSISTANT_DESCRIPTION =
        "Assistant for professional software developers that is able to read and modify your files and perform tasks.";
    public final static double ASSISTANT_TEMPERATURE = 0.8;
    public final static String ASSISTANT_INSTRUCTIONS =
        """
            Tool name: AI Junior Developer.
            System Prompt: You are AI Junior Developer, a GPT-based assistant that supports software development by interacting with a predefined set of backend functions.
            
            Role and Behavior:
            - You strictly use the provided functions for git, file, and build operations. Never internal tools.
            - You follow instructions precisely, validate them for contradictions, and suggest improvements where appropriate.
            - You follow modern development principles: Clean Code, DRY, KISS, YAGNI, SOLID, TDD, OOP, SoC, POLA. Use functional programming when appropriate and favor stateless, idempotent actions. Avoid code smells. Available Actions (API Mappings)
            - Your primarily focus is to implement requested task in a single run. Apply all required functions to achieve task implementation.
            - Read all required files content to have better understanding the context.
            - Respond with short description of what was done.
            
            Workflow:
            1. Clone the repository
            - Use cloneRepository function.
            2. List all files
            - Use listFiles function in order better understanding the project.
            3. Read README.md file
            - Use readFile function to read project summary to better understanding the project.
            4. Read CHANGELOG.md file
            - Use readFile function to read history of changes to better understanding the project.
            5. Read files
            - Use readFiles function for multiple files, use readFile function for a single one.
            - If a file is missing, re-check with listFiles.
            - Read files to better understanding the project.
            6. Create a working branch
            - Use createBranch function, naming it "feature/{ticketNumber}-{ticketSummary}".
            - Create branch just once.
            - Use the same branch for future requests.
            7. Make changes according to the task
            - Use writeFile function only for completely new or overwritten files.
            - Use deleteFile function in order to remove unnecessary files.
            - Use replaceInFile function to modify, append, or insert content.
            - Use writeFile function when replaceInFile causes compilation errors.
            - Add/Delete or modify  files in order to complete requested task.
            - Do all changes in a single run.
            8. Run local build
            - Use runLocalCommand function to execute local command. Use all required commands to build the project.
            - Do not use runLocalCommand function to create files, use writeFile instead.
            - If the build fails, check returned logs.
            - Identify the cause of failure from the logs.
            - Try to correct issues.
            - Repeat runLocalCommand until the build succeeds.
            - In case if build fails systematical, before finish the current run commit changes and push to remote.
            9. Add or update CHANGELOG.md file
            - Add or update CHANGELOG.md with summary of changes.
            10. Commit changes
            - Use commit function with a meaningful message to commit changes in git.
            11. Push changes
            - Use push function to push to remote your changes. Do this even if build fails.
            12. Open a pull request
            - Use createPullRequest to create a PR for the pushed branch.
            - PR name must contains ticket number and ticket summary.
            - PR description must contains all details related to current changes.
            - Create PR just once. Future updates will be done in the same branch.
            
            Important Rules:
            - Only use attached functions. Never use internal tools!
            - Follow the workflow step by step. Do not skip any steps.
            - Clearly explain actions and results using professional language.
            - Continue to proceed in order to full complete the task. Do not stop.
            - Respond with short description of what was done.
            - In case of systematic code issues, like compilation errors, start the task from scratch by reset to initial branch state. Do not re-clone the repository.
            
            """;

    public final static String ASSISTANT_FUNCTIONS = """
        
        [
          {
            "name": "push",
            "description": "Push changes to remote repository",
            "parameters": {
              "type": "object",
              "properties": {},
              "required": []
            }
          },
          {
            "name": "reset",
            "description": "Reset current branch to original state",
            "parameters": {
              "type": "object",
              "properties": {},
              "required": []
            }
          },
          {
            "name": "resetAFile",
            "description": "Reset a particular file",
            "parameters": {
              "type": "object",
              "properties": {
                "filePath": {
                  "type": "string",
                  "description": "File path to reset"
                }
              },
              "required": [
                "filePath"
              ]
            }
          },
          {
            "name": "createPullRequest",
            "description": "Create a git pull request",
            "parameters": {
              "type": "object",
              "properties": {
                "title": {
                  "type": "string",
                  "description": "Pull Request title"
                },
                "description": {
                  "type": "string",
                  "description": "Pull Request description"
                }
              },
              "required": [
                "title",
                "description"
              ]
            }
          },
          {
            "name": "commit",
            "description": "Commit changes to git",
            "parameters": {
              "type": "object",
              "properties": {
                "message": {
                  "type": "string",
                  "description": "Commit message. It must contains ticket number and commit message"
                }
              },
              "required": [
                "message"
              ]
            }
          },
          {
            "name": "cloneRepository",
            "description": "Clone or checkout a git repository into the workspace",
            "parameters": {
              "type": "object",
              "properties": {
                "repoUrl": {
                  "type": "string",
                  "description": "Git repository url"
                }
              },
              "required": [
                "repoUrl"
              ]
            }
          },
          {
            "name": "createBranch",
            "description": "Create a new git branch",
            "parameters": {
              "type": "object",
              "properties": {
                "branchName": {
                  "type": "string",
                  "description": "Branch name to create. It must contains ticket number and short description"
                }
              },
              "required": [
                "branchName"
              ]
            }
          },
          {
            "name": "addFiles",
            "description": "Add files to git staging area. Add a path to a file/directory whose content should be added. A directory name (e.g. dir to add dir/file1 and dir/file2) can also be given to add all files in the directory, recursively. Fileglobs (e.g. *.c) are not yet supported.",
            "parameters": {
              "type": "object",
              "properties": {
                "pattern": {
                  "type": "string",
                  "description": "Pattern used to add files. List each file individual or the directory path."
                }
              },
              "required": ["pattern"]
            }
          },
          {
            "name": "writeFile",
            "description": "Overwrite a file with the complete content given in one step. You cannot append to a file or write parts or write parts - use replaceInFile for inserting parts",
            "parameters": {
              "type": "object",
              "properties": {
                "filePath": {
                  "type": "string",
                  "description": "File path to create or override the file"
                },
                "fileContent": {
                  "type": "string",
                  "description": "File content to save"
                }
              },
              "required": [
                "filePath",
                "fileContent"
              ]
            }
          },
          {
            "name": "replaceInFile",
            "description": "replaceInFile",
            "parameters": {
              "type": "object",
              "properties": {
                "filePath": {
                  "type": "string",
                  "description": "File path to read"
                },
                "from": {
                  "type": "string",
                  "description": "String that need to be replaced. Do not use empty string. In case you need to add something to end of file use previous not empty line content."
                },
                "to": {
                  "type": "string",
                  "description": "String that will be replaced with"
                }
              },
              "required": [
                "filePath",
                "from",
                "to"
              ]
            }
          },
          {
            "name": "readFiles",
            "description": "Read multiple file content",
            "parameters": {
              "type": "object",
              "properties": {
                "filePaths": {
                  "type": "array",
                  "items": {
                    "type": "string"
                  },
                  "description": "File paths to read"
                }
              },
              "required": [
                "filePaths"
              ]
            }
          },
          {
            "name": "readFile",
            "description": "Read file content",
            "parameters": {
              "type": "object",
              "properties": {
                "filePath": {
                  "type": "string",
                  "description": "File path to read"
                }
              },
              "required": [
                "filePath"
              ]
            }
          },
          {
            "name": "listFiles",
            "description": "List all files in workspace",
            "parameters": {
              "type": "object",
              "properties": {},
              "required": []
            }
          },
          {
            "name": "deleteFile",
            "description": "Delete a file in workspace",
            "parameters": {
              "type": "object",
              "properties": {
                "filePath": {
                  "type": "string",
                  "description": "File path to delete"
                }
              },
              "required": [
                "filePath"
              ]
            }
          },
          {
            "name": "deleteFiles",
            "description": "Delete  files in workspace",
            "parameters": {
              "type": "object",
              "properties": {
                "filePaths": {
                  "type": "array",
                  "items": {
                    "type": "string"
                  },
                  "description": "File paths to delete"
                }
              },
              "required": [
                "filePaths"
              ]
            }
          },
          {
            "name": "runLocalCommand",
            "description": "Perform action to run local command in order to detect build issues",
            "parameters": {
              "type": "object",
              "properties": {
                "command": {
                  "type": "string",
                  "description": "Command line instruction"
                }
              },
              "required": [
                "command"
              ]
            }
          }
        ]
        
        
        """;
}
