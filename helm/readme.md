1. Edit `your-settings.yaml` and set the credentials as  explained here https://github.com/wwadge/awair-bridge
2. Install on a kubernetes cluster:

```
helm upgrade awairbridge ./awairbridge --install -f your-settings.yaml
```

3. Monitor:

```
kubectl get pods
kubectl logs -f <pod-name>
```
