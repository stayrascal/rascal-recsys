import os

import numpy as np
import pandas as pd
import tensorflow as tf
from matplotlib import pyplot as plt
from sklearn.model_selection import StratifiedKFold

from deepfm import config
from deepfm.data_reader import FeatureDictionary, DataParser
from deepfm.deep_fm import DeepFM
from deepfm.metrics import gini_norm


def load_data():
    train_df = pd.read_csv(config.TRAIN_FILE)
    test_df = pd.read_csv(config.TEST_FILE)

    def preprocess(df):
        cols = [c for c in df.columns if c not in ['id', 'target']]
        df["missing_feat"] = np.sum((df[cols] == -1).values, axis=1)
        df['ps_car_13_x_ps_reg_03'] = df['ps_car_13'] * df['ps_reg_03']
        return df

    train_df = preprocess(train_df)
    test_df = preprocess(test_df)

    cols = [c for c in train_df.columns if c not in ['id', 'target']]
    cols = [c for c in cols if (not c in config.IGNORE_COLS)]

    train_x = train_df[cols].values
    train_y = train_df['target'].values

    test_x = test_df[cols].values
    test_ids = test_df['id'].values

    features_cat_indices = [i for i, c in enumerate(cols) if c in config.CATEGORICAL_COLS]

    return train_df, test_df, train_x, train_y, test_x, test_ids, features_cat_indices


def run_base_model_dfm(train_df, test_df, folds, m_params_df):
    fd = FeatureDictionary(dfTrain=train_df,
                           dfTest=test_df,
                           numeric_cols=config.NUMERIC_COLS,
                           ignore_cols=config.IGNORE_COLS)
    data_parser = DataParser(feat_dict=fd)
    # Xi_train ：列的序号
    # Xv_train ：列的对应的值
    Xi_train, Xv_train, train_y = data_parser.parse(df=train_df, has_label=True)
    Xi_test, Xv_test, ids_test = data_parser.parse(df=test_df)

    print(train_df.dtypes)

    m_params_df['feature_size'] = fd.feat_dim
    m_params_df['field_size'] = len(Xi_train[0])

    y_train_meta = np.zeros((train_df.shape[0], 1), dtype=float)
    y_test_meta = np.zeros((test_df.shape[0], 1), dtype=float)

    _get = lambda x, l: [x[i] for i in l]

    gini_results_cv = np.zeros(len(folds), dtype=float)
    gini_results_epoch_train = np.zeros((len(folds), m_params_df['epoch']), dtype=float)
    gini_results_epoch_valid = np.zeros((len(folds), m_params_df['epoch']), dtype=float)

    for i, (train_idx, valid_idx) in enumerate(folds):
        Xi_train_, Xv_train_, y_train_ = _get(Xi_train, train_idx), _get(Xv_train, train_idx), _get(train_y, train_idx)
        Xi_valid_, Xv_valid_, y_valid_ = _get(Xi_train, valid_idx), _get(Xv_train, valid_idx), _get(train_y, valid_idx)

        dfm = DeepFM(**m_params_df)
        dfm.fit(Xi_train_, Xv_train_, y_train_, Xi_valid_, Xv_valid_, y_valid_)

        y_train_meta[valid_idx, 0] = dfm.predict(Xi_valid_, Xv_valid_)
        y_test_meta[:, 0] += dfm.predict(Xi_test, Xv_test)

        gini_results_cv[i] = gini_norm(y_valid_, y_train_meta[valid_idx])
        gini_results_epoch_train[i] = dfm.train_result
        gini_results_epoch_valid[i] = dfm.valid_result

    y_test_meta /= float(len(folds))

    # save result
    if m_params_df["use_fm"] and m_params_df["use_deep"]:
        clf_str = "DeepFM"
    elif m_params_df["use_fm"]:
        clf_str = "FM"
    elif m_params_df["use_deep"]:
        clf_str = "DNN"
    print("%s: %.5f (%.5f)" % (clf_str, gini_results_cv.mean(), gini_results_cv.std()))
    filename = "%s_Mean%.5f_Std%.5f.csv" % (clf_str, gini_results_cv.mean(), gini_results_cv.std())
    _make_submission(ids_test, y_test_meta, filename)

    _plot_fig(gini_results_epoch_train, gini_results_epoch_valid, clf_str)

    return y_train_meta, y_test_meta


def _make_submission(ids, y_pred, filename="submission.csv"):
    pd.DataFrame({"id": ids, "target": y_pred.flatten()}).to_csv(
        os.path.join(config.SUB_DIR, filename), index=False, float_format="%.5f")


def _plot_fig(train_results, valid_results, model_name):
    colors = ["red", "blue", "green"]
    xs = np.arange(1, train_results.shape[1] + 1)
    plt.figure()
    legends = []
    for i in range(train_results.shape[0]):
        plt.plot(xs, train_results[i], color=colors[i], linestyle="solid", marker="o")
        plt.plot(xs, valid_results[i], color=colors[i], linestyle="dashed", marker="o")
        legends.append("train-%d" % (i + 1))
        legends.append("valid-%d" % (i + 1))
    plt.xlabel("Epoch")
    plt.ylabel("Normalized Gini")
    plt.title("%s" % model_name)
    plt.legend(legends)
    plt.savefig("fig/%s.png" % model_name)
    plt.close()


dfm_params = {
    "use_fm": True,
    "use_deep": True,
    "embedding_size": 8,
    "dropout_fm": [1.0, 1.0],
    "deep_layers": [32, 32],
    "dropout_deep": [0.5, 0.5, 0.5],
    "deep_layer_activation": tf.nn.relu,
    "epoch": 30,
    "batch_size": 1024,
    "learning_rate": 0.001,
    "optimizer": "adam",
    "batch_norm": 1,
    "batch_norm_decay": 0.995,
    "l2_reg": 0.01,
    "verbose": True,
    "eval_metric": gini_norm,
    "random_seed": config.RANDOM_SEED

}

# load data
dfTrain, dfTest, X_train, y_train, X_test, ids_test, cat_features_indices = load_data()

# folds
folds = list(StratifiedKFold(n_splits=config.NUM_SPLITS,
                             shuffle=True,
                             random_state=config.RANDOM_SEED).split(X_train, y_train))

# y_train_dfm,y_test_dfm = run_base_model_dfm(dfTrain,dfTest,folds,dfm_params)
y_train_dfm, y_test_dfm = run_base_model_dfm(dfTrain, dfTest, folds, dfm_params)

# ------------------ FM Model ------------------
fm_params = dfm_params.copy()
fm_params["use_deep"] = False
y_train_fm, y_test_fm = run_base_model_dfm(dfTrain, dfTest, folds, fm_params)

# ------------------ DNN Model ------------------
dnn_params = dfm_params.copy()
dnn_params["use_fm"] = False
y_train_dnn, y_test_dnn = run_base_model_dfm(dfTrain, dfTest, folds, dnn_params)
