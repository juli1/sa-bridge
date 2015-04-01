package org.osate.importer.sysml.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.core.editor.IMultiDiagramEditor;
import org.eclipse.papyrus.infra.core.extension.commands.IModelCreationCommand;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.sasheditor.contentprovider.IPageManager;
import org.eclipse.papyrus.infra.core.services.ExtensionServicesRegistry;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationModel;
import org.eclipse.papyrus.sysml.blocks.Block;
import org.eclipse.papyrus.sysml.blocks.BlocksFactory;
import org.eclipse.papyrus.uml.diagram.wizards.category.DiagramCategoryDescriptor;
import org.eclipse.papyrus.uml.diagram.wizards.category.DiagramCategoryRegistry;
import org.eclipse.papyrus.uml.diagram.wizards.category.NewPapyrusModelCommand;
import org.eclipse.papyrus.uml.tools.model.UmlModel;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.xtext.ui.util.ResourceUtil;
import org.osate.aadl2.Element;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.instance.SystemOperationMode;
import org.osate.aadl2.modelsupport.errorreporting.AnalysisErrorReporterManager;
import org.osate.aadl2.util.OsateDebug;
import org.osate.importer.sysml.Activator;
import org.osate.ui.actions.AbstractInstanceOrDeclarativeModelModifyActionAction;
import org.osgi.framework.Bundle;

public final class DoExportModel extends AbstractInstanceOrDeclarativeModelModifyActionAction {

	private static IWorkbench workbench;

	protected Bundle getBundle() {
		return Activator.getDefault().getBundle();
	}

	protected String getMarkerType() {
		return "org.osate.importer.SysMLExporterMarker";
	}

	protected String getActionName() {
		return "SysML Exporter";
	}

	public static ServicesRegistry createServicesRegistry() {
		ServicesRegistry result = null;

		try {
			result = new ExtensionServicesRegistry(org.eclipse.papyrus.infra.core.Activator.PLUGIN_ID);
		} catch (org.eclipse.papyrus.infra.core.services.ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			OsateDebug.osateDebug("service registry error");
		}

		try {
			// have to create the model set and populate it with the DI model
			// before initializing other services that actually need the DI
			// model, such as the SashModel Manager service
			result.startServicesByClassKeys(ModelSet.class);
		} catch (Exception ex) {
			ex.printStackTrace();
			OsateDebug.osateDebug("service registry error");
			// Ignore this exception: some services may not have been loaded,
			// which is probably normal at this point
		}

		return result;
	}

	public static void createPapyrusModels(ModelSet modelSet, URI newURI) {
		RecordingCommand command = new NewPapyrusModelCommand(modelSet, newURI);
		getCommandStack(modelSet).execute(command);
	}

	public static final CommandStack getCommandStack(ModelSet modelSet) {
		return modelSet.getTransactionalEditingDomain().getCommandStack();
	}

	public static void initServicesRegistry(ServicesRegistry registry) throws ServiceException {
		try {
			registry.startRegistry();
		} catch (ServiceException ex) {
			// Ignore this exception: some services may not have been loaded,
			// which is probably normal at this point
		}

		registry.getService(IPageManager.class);
	}

	public static void initDomainModel(ModelSet modelSet, final URI newURI, String diagramCategoryId) {

		createEmptyDomainModel(modelSet, diagramCategoryId);

	}

	public static void createEmptyDomainModel(ModelSet modelSet, String diagramCategoryId) {
		try {
			IModelCreationCommand creationCommand = getDiagramCategoryMap().get(diagramCategoryId).getCommand();
			creationCommand.createModel(modelSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Map<String, DiagramCategoryDescriptor> getDiagramCategoryMap() {
		return DiagramCategoryRegistry.getInstance().getDiagramCategoryMap();
	}

	public static void initDiagrams(ModelSet modelSet, String categoryId) {
		initDiagrams(modelSet, null, categoryId);
	}

	public static void initDiagrams(ModelSet resourceSet, EObject root, String categoryId) {
		// FIXME we cannot properly set the root object
		UmlModel model = (UmlModel) resourceSet.getModel(UmlModel.MODEL_ID);
		EList<EObject> roots = model.getResource().getContents();
		if (!roots.isEmpty()) {

			root = roots.get(0);
		}
		// FIXME TO COMPLETE
	}

	public static void saveDiagram(ModelSet modelSet) {
		try {
			modelSet.save(new NullProgressMonitor());
		} catch (IOException e) {
			e.printStackTrace();
			// return false;
		}
	}

	public static void initDiagramModel(ModelSet modelSet, String categoryId) {
		initDiagrams(modelSet, categoryId);
		saveDiagram(modelSet);
	}

	public static void openDiagram(final URI newURI) {
//		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
//		if (page != null) {
//			try {
//				IEditorInput editorInput = createEditorInput(newURI);
//				IDE.openEditor(page, editorInput, getPreferredEditorID(editorInput), true);
//			} catch (PartInitException e) {
//				e.printStackTrace();
//			}
//		}
	}

//	public static IEditorInput createEditorInput(URI uri) {
//		return getSelectedStorageProvider().createEditorInput(uri);
//	}

	public static boolean createAndOpenPapyrusModel(URI newURI, String diagramCategoryId) {

		if (newURI == null) {
			return false;
		}

		ServicesRegistry registry = createServicesRegistry();
		if (registry == null) {
			return false;
		}

		try {
			// have to create the model set and populate it with the DI model
			// before initializing other services that actually need the DI
			// model, such as the SashModel Manager service
			ModelSet modelSet = registry.getService(ModelSet.class);

			createPapyrusModels(modelSet, newURI);

			initServicesRegistry(registry);

			initDomainModel(modelSet, newURI, diagramCategoryId);

			initDiagramModel(modelSet, diagramCategoryId);

			openDiagram(newURI);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				registry.disposeRegistry();
			} catch (ServiceException ex) {
				ex.printStackTrace();
			}
		}

		return true;
	}

	private static URI getDiagramURI(SystemInstance si) {
		IFile file = ResourceUtil.getFile(si.eResource());
		IPath path = file.getFullPath();
		return URI.createPlatformResourceURI(path.addFileExtension("sysml").toString(), true);
	}

//
//	public static Resource createDiagram(final Object arg, URI targetURI) {
//		TransactionalEditingDomain editingDomain = GMFEditingDomainFactory.INSTANCE.createEditingDomain();
//
//		final Resource modelResource = editingDomain.getResourceSet().createResource(modelURI);
//		final String diagramName = diagramURI.lastSegment();
//		AbstractTransactionalCommand command = new AbstractTransactionalCommand(editingDomain,
//				Messages.DcaseDiagramEditorUtil_CreateDiagramCommandLabel, Collections.EMPTY_LIST) {
//			protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info)
//					throws ExecutionException {
//				attachModelToResource(arg, modelResource);
//
//				Diagram diagram = ViewService.createDiagram(arg, ArgumentEditPart.MODEL_ID,
//						DcaseDiagramEditorPlugin.DIAGRAM_PREFERENCES_HINT);
//				if (diagram != null) {
//					diagramResource.getContents().add(diagram);
//					diagram.setName(diagramName);
//					diagram.setElement(arg);
//				}
//
//				try {
//					modelResource.save(
//							net.dependableos.dcase.diagram.part.DcaseDiagramEditorUtil.getSaveOptions());
//				} catch (IOException e) {
//
//					DcaseDiagramEditorPlugin.getInstance().logError("Unable to store model and diagram resources", e); //$NON-NLS-1$
//				}
//				return CommandResult.newOKCommandResult();
//			}
//		};
//		try {
//			OperationHistoryFactory.getOperationHistory().execute(command, null, null);
//		} catch (ExecutionException e) {
//			DcaseDiagramEditorPlugin.getInstance().logError("Unable to create model and diagram", e); //$NON-NLS-1$
//		}
//		setCharset(WorkspaceSynchronizer.getFile(modelResource));
//		setCharset(WorkspaceSynchronizer.getFile(diagramResource));
//		return diagramResource;
//	}

	public void init(IWorkbenchWindow window) {
		this.workbench = workbench;

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void analyzeDeclarativeModel(IProgressMonitor monitor, AnalysisErrorReporterManager errManager,
			Element declarativeObject) {
		OsateDebug.osateDebug("Export an Declarative Model");

	}

	@Override
	protected void analyzeInstanceModel(IProgressMonitor monitor, AnalysisErrorReporterManager errManager,
			SystemInstance root, SystemOperationMode som) {
		OsateDebug.osateDebug("Export an Instance Model");
//		Block block = BlocksFactory.eINSTANCE.createBlock();
		URI modelURI = getDiagramURI(root);
//
//		OsateDebug.osateDebug("Target=" + modelURI);
//		createAndOpenPapyrusModel(modelURI, "sysml");

//		return;
//

		// String modelName = "model";

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {

				try {

					String modelName = "model";

					String diagramName = "NewDiagram";
					EclipseProject eclipseProject = new EclipseProject("test");
					eclipseProject.copyResources("org.osate.importer.sysml", modelName + ".di",
							modelName + ".notation", modelName + ".uml");
					ProgramaticPapyrusEditor editorHandler = new ProgramaticPapyrusEditor(eclipseProject, modelName);
					IMultiDiagramEditor editor = editorHandler.getEditor();

//				assertNotNull("editor created", editor); 

					NotationModel notationModel = (NotationModel) editorHandler.getModelSet().getModel(
							NotationModel.MODEL_ID);
//				assertNotNull("notation model loaded", notationModel);

					// If the following assertion is false, this means that the model is not loaded from file (it has been created)
					Diagram diagram = notationModel.getDiagram(diagramName);
//				assertNotNull("diagram loaded", diagram);

					for (Resource res : notationModel.getResources()) {
						OsateDebug.osateDebug("res=" + res);

					}

					ModelSet ms = editorHandler.getModelSet();
					Resource resToLoad;
					resToLoad = null;
					for (final Resource res : ms.getResources()) {
						if (res.getURI().toString().endsWith("model.uml")) {
							OsateDebug.osateDebug("has to load=" + res);
							resToLoad = res;
						}
//						OsateDebug.osateDebug("ms res2=" + res);
					}
//					resToLoad.unload();
					if (resToLoad != null) {
//						resToLoad.unload();
						fillUMLResource(resToLoad, editorHandler);

					}

//					resToLoad.setModified(true);
//					editorHandler.dispose();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});

	}

	public static void printResourceContents(final EObject eo, int level) {
		for (int i = 0; i < level; i++) {
			System.out.print(" ");

		}
		System.out.print("Object " + eo);
		if (eo instanceof org.eclipse.uml2.uml.Class) {
			org.eclipse.uml2.uml.Class cl = (org.eclipse.uml2.uml.Class) eo;
			System.out.print("class" + cl);
		}

		System.out.print("\n");
		for (EObject child : eo.eContents()) {
			printResourceContents(child, level + 3);

		}
	}

	public static void updatePackage(org.eclipse.uml2.uml.Package pkg) {

	}

	public static void updateModel(org.eclipse.uml2.uml.Model model) {

		for (int i = 0; i < model.getPackagedElements().size(); i++) {
			PackageableElement pkg = (PackageableElement) model.getPackagedElements().get(i);

			System.out.println("PackageableElement " + pkg);

			org.eclipse.uml2.uml.Class newClass = UMLFactory.eINSTANCE.createClass();
			org.eclipse.uml2.uml.Package newUMLPackage = UMLFactory.eINSTANCE.createPackage();

			PackageableElement pe = model.createPackagedElement("toto", pkg.eClass());
			pe.setName("toto");
//			model.eContents().add(pe);
			return;
		}
	}

	public static void updateEObject(EObject eo) {

//		System.out.println("Object " + eo);

		if (eo instanceof org.eclipse.uml2.uml.Model) {
			updateModel((org.eclipse.uml2.uml.Model) eo);
		}
		for (EObject child : eo.eContents()) {
			updateEObject(child);

		}
	}

	static Block newBlock;

	public static void fillUMLResource(final Resource res, ProgramaticPapyrusEditor editorHandler) {
		OsateDebug.osateDebug("UML resource=" + res);
		for (EObject eo : res.getContents()) {
//			OsateDebug.osateDebug("eo=" + eo);
			printResourceContents(eo, 0);

		}
		TransactionalEditingDomain editingDomain;
		newBlock = BlocksFactory.eINSTANCE.createBlock();
		try {
			editingDomain = editorHandler.getTransactionalEditingDomain();

			final CommandStack commandStack = editingDomain.getCommandStack();
//			commandStack.execute(new RecordingCommand(editingDomain) {
//
//				@Override
//				protected void doExecute() {
//					// Save DiagramDialog at proper position
//
////					res.getContents().add(newBlock);
//
////				((DocumentRoot) resource.getContents().get(0)).getProcedure().get(0).add((DiagramDialog ) obj);
//				}
//			});

			commandStack.execute(new RecordingCommand(editingDomain) {

				@Override
				protected void doExecute() {
					// Save DiagramDialog at proper position

					for (EObject eo : res.getContents()) {
//						OsateDebug.osateDebug("eo=" + eo);
						updateEObject(eo);

					}
					res.setModified(true);

//				((DocumentRoot) resource.getContents().get(0)).getProcedure().get(0).add((DiagramDialog ) obj);
				}
			});
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		OsateDebug.osateDebug("UML resource after=" + res);
		for (EObject eo : res.getContents()) {
//			OsateDebug.osateDebug("eo=" + eo);
			printResourceContents(eo, 0);

		}
		try {
			res.load(null);
			File file = new File("/tmp/newfile.txt");
			FileOutputStream fop = new FileOutputStream(file);
			res.save(fop, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
