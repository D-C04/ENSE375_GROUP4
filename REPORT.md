<img width="975" height="166" alt="image" src="https://github.com/user-attachments/assets/137b543c-9729-4cad-b8b8-7d2b15c4f9e2" />

# ENSE 375 – Software Testing and Validation

<div align="center">
<h1> <b> HABIT TRACKER </b> </h1>
</div>  

## Team Members
#### Abrianna Primavera (200504635)
#### Bushra Alkhateeb (200525581)
####  Deran Cross (200513336)

## Table of Contents

### 2 ...Design Problem
- 2.1 Problem Definition
- 2.2 Design Requirements  
  - 2.2.1 Functions
  - 2.2.2 Objectives
  - 2.2.3 Constraints
 
### 3 ... Solution
- 3.1 Solution 1
- 3.2 Solution 2
- 3.3 Final Solution

### 4 ...Team Work
- 4.1 Meeting 1 : Brainstorming
- 4.2 Meeting 2 : Problem Definition
- 4.3 Meeting 3 : Project Functions, Objectives and Constraints
- 4.4 Meeting 4 :  Discuss Possible Project Solutions
- 4.5 Meeting 5 : Outline and Finalize Solutions
- 4.6 Meeting 6 : Complete MVCs, Discuss Implementation and GitHub Update

### 5 ...Project Management

# 2.	Design Problem  
## 2.1	Problem Definition    

Balancing hobbies and everyday necessities is a difficult task for many individuals due to several factors, including being unable to measure growth, maintain focus or manage resources.   

This application will allow users to have an instinctive way to track the hours spent on a hobby, have visual representation of their progress as well as provide hobby specific milestones and resources.  

## 2.2 Design Requirements  

### 2.2.1 Functions  

#### User authentication and account:  
- The project/system shall allow users to sign up and login with the required credentials  
- The system will allow a user into their account only if their credentials match  
- The system shall allow for user managed data deletion  

#### Manage habits/tasks:  
- The program shall allow users to add, remove, and edit habits  
- The program shall allow users to track habits and progress via KPIs, diagrams, and graphs  
- The program shall allow users to update daily habits/ tasks   
- The program will not limit what the user can set as their goal/task  

#### Dashboard:  
- The program shall allow the user to enter to a dashboard that showcases their new habits/tasks, old tasks, completed tasks, and uncompleted tasks  
- The program shall show the user a calendar which includes upcoming due dates, uncompleted tasks and more.  
- The program will allow users to log daily notes related to their goal progress and experience  
- The program will allow users to view their habit history  

#### Notification and reminders:  
- The program will notify users when they have reached a target  
- The program will remind users when deadlines are near or missed  
 
#### Streaks:  
- The program will retain data of consecutive logged habits and allow users to view it  

#### User Profile  
- The program will allow users to create and modify a user profile with personal information such as name, age, etc.  

### 2.2.2 Objectives (5 Ws)  

The main objective of this program is to provide users with a complete interface/program which aids them to consistently track daily tasks and habits, and measure their progress.  

Other objectives include giving users a space that motivates them to complete tasks and commit to completing daily habits they wish to change and incorporate in their daily life.
  #### Who?
  -  The application’s primary users will be those who wish to monitor their progress towards adopting a new habit or completing a goal.
           
  #### What?   
  - The aim of this application is to allow users to build and maintain their habits through tracking tools, reminders and analytics.
        The simple and intuitive interface will encourage consistent usage and support long-term behavioral change.
          
  #### Why?  
  -  To create an informative platform for users to balance everyday responsibilities with their hobbies and completing related tasks.
    The main purpose is to motivate and encourage users to start new hobbies, stay organized and complete tasks that are pending to be completed. 
      
  #### Where?   
  - The technology stack will consist of front-end technologies including: Java, JavaScript, HTML, CSS and more.
  - The application will be deployed on localhost  
     
  #### When?   
  - The team will have a time frame of four months to complete this project; between September 2026 until the end of November 2026

### 2.2.3 Constraints  

### (a) Design Constraints:  

#### Economic factors
- We will not spend any money on the creation of this project.
- We will not make any money off of this project  
- The full application must be free to all users

#### Regulatory compliance (Security and Access)  
- Users must not be able to access the accounts or information of other users    
- Passwords must be stored securely    
- User information must be stored securely    

#### Reliability
- The application must work as intended  
- Application navigation paths must be correct, consistent, and intuitive  
- The application must allow actions must be completed within a certain amount of time  
- Habit tracking information must always be accurate and consistent. Incorrect or corrupted data must not be shown to the user.    

#### Sustainability and Environmental Factors  
- AI use must be limited to lessen the project’s environmental impact    
- Coding must be optimized to improve efficiency and minimize resource usage   

##### Ethics
- User information must not be shared publicly or privately  
- User information must not be accessible by the developers of the application  
- The type of goal/habit the user wants to track will not be limited or censored by the application

### (b) General Constraints:  

#### Technical constraints (technology stack)
We are constricted to using a specific type of technology stack which includes front end technologies (HTML, CSS, JavaScript) and backend technologies (MongoDB, NodeJs, ect)

#### Time 
Due to tight schedules of team members, and a tight time frame between the months of September through November, we have to come up with a team-work strategy that should allow us to complete the deliverables on time, and within the deadlines.   

#### Knowledge
Our knowledge regarding software and use of technology stacks will be a great constraint here, because we are constricted to using our previous knowledge and programming abilities we attained from previous classes and experience to build our project.

# 3. Solution
### 3.1 ... Solution 1   

Solution 1 is a task tracker built with minimal features: users can add hobbies, mark daily completion, and view daily
achievements. The layout relies on basic input validation and data storage. This prototype is simple and meets some feature requirements, but it is not very robust.  
  
The reasons for not selecting this solution include the following  

#### - Boundary Value Testing  
The design was not made with the idea of restricting input on time, hobbies  

#### - Unit testing is difficult  
Due to the design simplicity, multiple sections, such as achievements, make unit testing difficult as their behaviours were too tightly depended on other sections of the design  

#### -  Limited error handling paths  
The prototype lacked a structure that would allow for multiple user errors to be tested. Error paths mainly arose through user validation and not data input.  

#### - State Transition Testing  
Undefined state transitions make this type of testing difficult. The status of hobbies is simplified to ‘Not Started’ and ‘Completed’ with no real design outlining transition rules  

#### - Equivalence Class Testing  
Difficulties arise due to input classes not being properly defined

This solution was functional but not test friendly enough to be used as a final solution.  

### 3.2 ... Solution 2  
Solution 2 is an enhanced task tracker design that allows for user validation, status and statistics tracking, an analytical dashboard and achievement tracking. Other functionalities include an edit and delete functionality.   

Solution 2 addresses the issues outlined in Solution one in various one.

#### - More Modular Features
The difficulties in unit and equivalence class testing are addressed due to Solution 2 being able to be separated in different modules. For example: Hobby Management, Activity Logging. Progress Tracking, etc. This design allows for better boundary value testing for areas such as user account creation and login, as well as activity logging.   

#### - Clearer Data Flow  
The design allows for better integration testing and error handling in comparison to Solution 1 because the path of data flow is outlined and designed more comprehensively.  

#### - More Consistent State Transitions  
Solution 2 allows for dedicated modules dedicated to the status of user activities or tasks with defined transitions

Solution 2, however, still has deficiencies. Some dependiencies still exist between modules, such as logging activites, tracking progress and analytics.


### 3.3 ... Final Solution  
<img width="958" height="721" alt="IMG_4785" src="https://github.com/user-attachments/assets/7098b792-552d-4edc-bc4c-0b5e475b65cb" />

The final solution is outlined with MVC architecture and sufficiently addresses the deficiencies in both Solutions 1 and 2.  
Solution three allows for well defined units, reducing dependencies and therefore supports more comprehensive testing.

#### - Shared Data Layer  
With the idea of a supporting database and the MVC architecture, Solution 3 decouples modules and allows for activity logging, achievements and analytics to be tested without involving each other.  

# 4.	Team Work
## 4.1	...Meeting 1  

Time: September 10, 2026,  3:00 pm to 3:45 pm  
Agenda: Project Brainstorming
| Team Member | Previous Task | Completion State | Next Task |
|----------|----------|----------|----------|
| Abrianna Primavera   | NA  | NA  | Create a suitable problem definition |
| Bushra Alkhateeb | NA  | NA  | Create a suitable problem definition |
|  Deran Cross        |   NA       |  NA        |    Create a suitable problem definition      |

## 4.2	...Meeting 2  

Time: September 15, 2026, 3:45 pm to 4:20 pm  
Agenda: Problem Defintion
| Team Member | Previous Task | Completion State | Next Task |
|----------|----------|----------|----------|
| Abrianna Primavera   | Create a suitable problem definition | Complete  | Define project functions  |
| Bushra Alkhateeb | Create a suitable problem definition | Complete  | Define project objectives |
|  Deran Cross        |    Create a suitable problem definition      | Complete         |  Define project constraints       |

## 4.3	...Meeting 3  

Time: September 23, 2026, 3:45 pm to 4:20 pm  
Agenda: Discuss project functions, objectives and constraints
| Team Member | Previous Task | Completion State | Next Task |
|----------|----------|----------|----------|
| Abrianna Primavera   | Define project functions | Complete  | Brainstorm solutions |
| Bushra Alkhateeb | Define project objectives | Complete  | Brainstorm solutions |
|  Deran Cross        |    Define project constraints       | Complete         | Brainstorm solutions |

## 4.4	...Meeting 4  

Time: October 2, 2026, 3:30 pm to 4:30 pm  
Agenda: Discuss possible project solutions
| Team Member | Previous Task | Completion State | Next Task |
|----------|----------|----------|----------|
| Abrianna Primavera   | Brainstorm solutions | Complete  | Create UML Class Diagrams for Solutions |
| Bushra Alkhateeb | Brainstorm solutions | Complete  | Create UML Class Diagrams for Solutions |
|  Deran Cross        |    Brainstorm solutions       | Complete         | Define Solution Testing Deficiencies |

## 4.5	...Meeting 5  

Time: October 7, 2026, 1:00 pm to 2:15 pm  
Agenda: Outline and Finalize Solutions
| Team Member | Previous Task | Completion State | Next Task |
|----------|----------|----------|----------|
| Abrianna Primavera   | Create UML Class Diagrams for Solutions | Complete  | Create MVC Diagram |
| Bushra Alkhateeb | Create UML Class Diagrams for Solutions | Complete  | Create MVC Diagram |
|  Deran Cross        |    Define Solution Testing Deficiencies      | Complete        | Create MVC Diagram |  

## 4.6	...Meeting 6    

Time: October 8, 2026 10:30 am to 11: 15 am  
Agenda:  Complete MVCs, Discuss Implementation and GitHub Update
| Team Member | Previous Task | Completion State | Next Task |
|----------|----------|----------|----------|
| Abrianna Primavera   | Create MVC Diagram | Complete  | Final Design Implementation and Testing |
| Bushra Alkhateeb | Create MVC Diagram  | Complete  | Final Design Implementation and Testing |
|  Deran Cross        |    Create MVC Diagram       | Complete         | Final Design Implementation and Testing |  

# 5.	Project Management

Here is a link to our Gantt Chart which showcases the progress of our work:(https://1drv.ms/x/c/2547ef4451b03ad2/IQB8uDNAQz0YT7Q_k9PeiQKWARacsVqA9pwwGyO-VtNzUmY?e=3EBakP)


