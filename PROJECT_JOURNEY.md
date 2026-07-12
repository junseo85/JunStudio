1. Project Conception & Requirements
   Goal: Build a lesson management tool for a music studio.

Key Challenges: Handling secure user authentication and integrating a real-world payment gateway (Stripe).

2. Development Phase (Code Process)
   Backend: I chose Spring Boot for its robust security framework. I implemented JWT (JSON Web Tokens) to handle stateless sessions, crucial for cloud deployments.

Frontend: Created a responsive UI using React. I focused on user experience, ensuring students could book lessons in under three clicks.

Database: Structured MySQL to handle a one-to-many relationship between instructors and students, with foreign key constraints for lesson history.

3. Deployment & Cloud Engineering (The "Hard" Part)
   This section is your strongest selling point. It shows you can solve professional-level bugs.

Containerization: Wrote a Dockerfile to package the Spring Boot app, ensuring consistency between my local Windows development environment and the Linux cloud.

Kubernetes Migration: Transitioned from local development to AWS EKS. I specifically chose AWS Fargate (Serverless) to avoid managing individual EC2 nodes.

Debugging Story (The "DescribeListenerAttributes" Incident):

The Problem: The public URL was stuck in <pending> state due to a 403 Access Denied error.

The Investigation: Used kubectl logs and kubectl describe to pinpoint a version mismatch in the IAM security policy.

The Resolution: I updated the IAM OIDC provider, created a "V3" security policy with modern DescribeListenerAttributes permissions, and successfully provisioned a public Network Load Balancer.

4. Security & Best Practices
   Stripe Integration: Implemented Webhooks to ensure that the application only grants lesson access after Stripe confirms a successful payment event.

Environment Safety: Leveraged .env files and Kubernetes Secrets to ensure no API keys or database passwords were ever committed to GitHub.